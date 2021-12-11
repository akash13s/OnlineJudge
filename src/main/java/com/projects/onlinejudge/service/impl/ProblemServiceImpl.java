package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.core.Runner;
import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.TestCase;
import com.projects.onlinejudge.domain.User;
import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.dto.SampleTestCaseDTO;
import com.projects.onlinejudge.dto.TestCaseDTO;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.repository.TestCaseRepository;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.ProblemService;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private Runner runner;

    @Override
    public ProblemDTO createProblem(ProblemDTO problemDTO) {
        User user = validateUser(problemDTO.getUserName());
        String problemCode = problemDTO.getProblemCode();
        Problem existingProblem = problemRepository.findProblemByProblemCode(problemCode);
        if (Objects.nonNull(existingProblem)) {
            // raise ProblemCodeAlreadyExistsException
        }
        Problem problem = mapper.map(problemDTO, Problem.class);
        problem.setUser(user);
        problem.setCreatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        user.getUserProblems().add(problem);
        userRepository.save(user);
        return mapper.map(problem, ProblemDTO.class);
    }

    @Override
    public ProblemDTO getProblem(String problemCode) {
        Problem problem = validateProblemCode(problemCode);
        return mapper.map(problem, ProblemDTO.class);
    }

    @Override
    public ProblemDTO updateProblem(ProblemDTO problemDTO) {
        User user = validateUser(problemDTO.getUserName());
        Problem problem = validateProblemCode(problemDTO.getProblemCode());
        problem.setConstraints(problemDTO.getConstraints());
        problem.setDescription(problemDTO.getDescription());
        problem.setInputFormat(problemDTO.getInputFormat());
        problem.setOutputFormat(problemDTO.getOutputFormat());
        problem.setTitle(problemDTO.getTitle());
        problem.setUpdatedAt(Date.from(Instant.now()));
        Problem problem1 = problemRepository.save(problem);
        return mapper.map(problem1, ProblemDTO.class);
    }

    @Override
    public Boolean deleteProblem(String problemCode) throws IOException {
        if (!deleteAllTestCases(problemCode)) {
            return false;
        }
        problemRepository.delete(problemRepository.findProblemByProblemCode(problemCode));
        return true;
    }

    @Override
    public TestCaseDTO addTestCase(String problemCode, MultipartFile inputFile, MultipartFile outputFile,
                                   boolean isSampleTest) throws IOException, InterruptedException {

        // add problem related logic
        Problem problem = validateProblemCode(problemCode);
        String inputFileKey = getFileName(problemCode, true, problem.getNextTestCaseId(), isSampleTest);
        String outputFileKey = getFileName(problemCode, false, problem.getNextTestCaseId(), isSampleTest);

        // final test case object returned from DB
        TestCase testCase1;

        /*
        - For addition:
            - See if problem directory is already present in LFS.
                - If yes, check if the lastModified time for local problem test case directory>= lastModified time for s3 problem test case directory
                    - If yes, add to s3, db, and LFS
                    - If no, then add to s3, db and download the new directory from s3
                - If no, add in s3, db.
         */
        String problemTestCaseDirectory = runner.getProblemTestCasesDirectory() + FileConstants.PROBLEMS + "/" + problem.getProblemCode();
        if (Files.exists(Path.of(problemTestCaseDirectory))) {
            Date testCaseLastUpdatedAt = problem.getTestCaseLastUpdatedAt();
            File dir = new File(problemTestCaseDirectory);
            long localProblemTestCaseDirectoryLastUpdatedAt = getLastModifiedDateForProblemTestCaseFolder(problemTestCaseDirectory);
            if (testCaseLastUpdatedAt.after(Date.from(Instant.ofEpochMilli(localProblemTestCaseDirectoryLastUpdatedAt)))) {
                // upload to s3 and add entry in db - this sequence needs to be maintained
                testCase1 = addTestCaseToS3AndDB(problem, inputFileKey, outputFileKey, isSampleTest, inputFile, outputFile);

                // download new problem test case directory in LFS
                FileUtils.deleteDirectory(dir);
                amazonClient.downloadDirectory(FileConstants.PROBLEMS + "/" + problem.getProblemCode(), runner.getProblemTestCasesDirectory());
            }
            else {
                // add to s3, db and then add to LFS
                testCase1 = addTestCaseToS3AndDB(problem, inputFileKey, outputFileKey, isSampleTest, inputFile, outputFile);

                String inputFilePath = runner.getProblemTestCasesDirectory() + inputFileKey;
                File inputTestFile = new File(inputFilePath);
                inputTestFile.createNewFile();

                String outputFilePath = runner.getProblemTestCasesDirectory() + outputFileKey;
                File outputTestFile = new File(outputFilePath);
                outputTestFile.createNewFile();

                amazonClient.downloadFile(inputFileKey, inputFilePath);
                amazonClient.downloadFile(outputFileKey, outputFilePath);
            }
        }
        else {
            // only add to s3 and db
            testCase1 = addTestCaseToS3AndDB(problem, inputFileKey, outputFileKey, isSampleTest, inputFile, outputFile);
        }

        if (Objects.isNull(testCase1)) {
            return null;
        }

        if (isSampleTest) {
            return mapper.map(testCase1, SampleTestCaseDTO.class);
        }
        else {
            return mapper.map(testCase1, TestCaseDTO.class);
        }

    }

    @Override
    public Boolean deleteTestCase(String problemCode, int testCaseId, boolean isSampleTest) throws IOException, InterruptedException {
        // add problem related logic
        Problem problem = validateProblemCode(problemCode);
        String inputFileKey = getFileName(problemCode, true, testCaseId, isSampleTest);
        String outputFileKey = getFileName(problemCode, false, testCaseId, isSampleTest);

        boolean success;

        /*
        - For deletion:
            - See if problem directory is already present in LFS.
                - If no, remove from s3, db.
                - If yes, check if the lastModified time for local problem test case directory>= lastModified time for s3 problem test case directory
                    - If yes, remove from s3, db, and LFS
                    - If no, then remove from s3, db and download the new directory from s3
         */
        String problemTestCaseDirectory = runner.getProblemTestCasesDirectory() + FileConstants.PROBLEMS + "/" + problem.getProblemCode();

        if (Files.exists(Path.of(problemTestCaseDirectory))) {
            Date testCaseLastUpdatedAt = problem.getTestCaseLastUpdatedAt();
            File dir = new File(problemTestCaseDirectory);
            long localProblemTestCaseDirectoryLastUpdatedAt = getLastModifiedDateForProblemTestCaseFolder(problemTestCaseDirectory);
            if (testCaseLastUpdatedAt.after(Date.from(Instant.ofEpochMilli(localProblemTestCaseDirectoryLastUpdatedAt)))) {
                // delete from s3, db and download the new test case directory
                success = removeTestCaseFromS3AndDB(problem, testCaseId, inputFileKey, outputFileKey);

                // download new problem test case directory in LFS
                FileUtils.deleteDirectory(dir);
                amazonClient.downloadDirectory(FileConstants.PROBLEMS + "/" + problem.getProblemCode(), runner.getProblemTestCasesDirectory());
            }
            else {
                // delete from s3, db and LFS
                success = removeTestCaseFromS3AndDB(problem, testCaseId, inputFileKey, outputFileKey);

                String inputFilePath = runner.getProblemTestCasesDirectory() + inputFileKey;
                File inputTestFile = new File(inputFilePath);
                inputTestFile.delete();

                String outputFilePath = runner.getProblemTestCasesDirectory() + outputFileKey;
                File outputTestFile = new File(outputFilePath);
                outputTestFile.delete();

            }
        }
        else {
            // remove only from s3 and db
            success = removeTestCaseFromS3AndDB(problem, testCaseId, inputFileKey, outputFileKey);
        }

        return success;
    }

    private Boolean deleteAllTestCases(String problemCode) throws IOException {
        Problem problem = validateProblemCode(problemCode);
        // delete all test cases from s3 and entries from db
        boolean success = amazonClient.deleteDirectory(FileConstants.PROBLEMS + "/" + problemCode);
        if (!success) {
            return false;
        }
        testCaseRepository.deleteAll(problem.getTestCases());

        // delete problem test case directory if it exists in LFS
        String problemTestCaseDirectory = runner.getProblemTestCasesDirectory() + FileConstants.PROBLEMS + "/" + problemCode;

        if (Files.exists(Path.of(problemTestCaseDirectory))) {
            File dir = new File(problemTestCaseDirectory);
            FileUtils.deleteDirectory(dir);
        }

        return true;
    }


    private String getFileName(String problemCode, boolean isInputFile, long testCaseNumber,
                               boolean isSampleTest) {
        String fileName = FileConstants.PROBLEMS + "/";
        fileName += problemCode;
        if (isSampleTest) {
            fileName += FileConstants.SAMPLE_TEST;
        }
        else {
            fileName += FileConstants.HIDDEN_TEST;
        }
        return fileName.concat(isInputFile? FileConstants.INPUT_FILE: FileConstants.OUTPUT_FILE) + testCaseNumber +
                FileConstants.TEXT_FILE_EXT;
    }

    private User validateUser(String userName) {
        User user = userRepository.findUserByUserName(userName);
        if (Objects.isNull(user)) {
            // raise custom exception - UserNameDoesNotExistException
        }
        return user;
    }

    private Problem validateProblemCode(String problemCode) {
        Problem problem = problemRepository.findProblemByProblemCode(problemCode);
        if (Objects.isNull(problem)) {
            // raise ProblemCodeDoesNotExistException custom exception
        }
        return problem;
    }

    private boolean uploadTestCaseToS3(String inputFileKey, MultipartFile inputFile,
                                       String outputFileKey, MultipartFile outputFile) {
        return amazonClient.uploadFile(inputFileKey, inputFile) && amazonClient.uploadFile(outputFileKey, outputFile);
    }

    private TestCase addTestCaseToDB(Problem problem, String inputFileKey, String outputFileKey, boolean isSampleTest,
                             MultipartFile inputFile, MultipartFile outputFile) throws IOException, InterruptedException {
        TestCase testCase = new TestCase();
        testCase.setId(problem.getNextTestCaseId());
        testCase.setProblemCode(problem.getProblemCode());
        testCase.setInputFileName(inputFileKey);
        testCase.setOutputFileName(outputFileKey);
        testCase.setSampleTest(isSampleTest);
        if (testCase.isSampleTest()) {
            // convert multipart file content to string
            testCase.setSampleInput(new String(inputFile.getBytes()));
            testCase.setSampleOutput(new String(outputFile.getBytes()));
        }

        testCase.setProblem(problem);
        TestCase testCase1 = testCaseRepository.save(testCase);

        problem.setTestCaseLastUpdatedAt(Date.from(Instant.now()));
        problem.setNextTestCaseId(problem.getNextTestCaseId() + 1);
        problem.setNumberOfTestCases(problem.getNumberOfTestCases() + 1);
        problem.setUpdatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        TimeUnit.SECONDS.sleep(1);

        return testCase1;
    }

    private TestCase addTestCaseToS3AndDB(Problem problem, String inputFileKey, String outputFileKey, boolean isSampleTest,
                                          MultipartFile inputFile, MultipartFile outputFile) throws IOException, InterruptedException {
        boolean success = uploadTestCaseToS3(inputFileKey, inputFile, outputFileKey, outputFile);
        if (!success) {
            return null;
        }
        return addTestCaseToDB(problem, inputFileKey, outputFileKey, isSampleTest, inputFile, outputFile);
    }

    private boolean removeTestCaseFromS3(String inputFileKey, String outputFileKey) {
        return amazonClient.deleteFile(inputFileKey) && amazonClient.deleteFile(outputFileKey);
    }

    private void removeTestCaseFromDB(Problem problem, long testCaseId) throws InterruptedException {
        TestCase testCase = testCaseRepository.findByProblemAndId(problem, testCaseId);
        testCaseRepository.delete(testCase);

        problem.setTestCaseLastUpdatedAt(Date.from(Instant.now()));
        problem.setNumberOfTestCases(problem.getNumberOfTestCases() - 1);
        problem.setUpdatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        TimeUnit.SECONDS.sleep(1);
    }

    private boolean removeTestCaseFromS3AndDB(Problem problem, long testCaseId, String inputFileKey, String outputFileKey) throws InterruptedException {
        boolean success = removeTestCaseFromS3(inputFileKey, outputFileKey);
        if (!success) {
            return false;
        }
        removeTestCaseFromDB(problem, testCaseId);
        return true;
    }

    private long getLastModifiedDateForProblemTestCaseFolder(String problemTestCaseDirectory) {
        File hiddenTestCases = new File(problemTestCaseDirectory + FileConstants.HIDDEN_TEST);
        File sampleTestCases = new File(problemTestCaseDirectory + FileConstants.SAMPLE_TEST);
        return Math.max(hiddenTestCases.lastModified(), sampleTestCases.lastModified());
    }

}
