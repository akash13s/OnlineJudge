package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.config.LanguageConfig;
import com.projects.onlinejudge.constants.FileConstants;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

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
    public Boolean deleteProblem(String problemCode) {
        if (!deleteAllTestCases(problemCode)) {
            return false;
        }
        problemRepository.delete(problemRepository.findProblemByProblemCode(problemCode));
        return true;
    }

    @Override
    public TestCaseDTO addTestCase(String problemCode, MultipartFile inputFile, MultipartFile outputFile,
                                   boolean isSampleTest) throws IOException {
        // add problem related logic
        Problem problem = validateProblemCode(problemCode);
        String inputFileKey = getFileName(problemCode, true, problem.getNextTestCaseId(), isSampleTest);
        String outputFileKey = getFileName(problemCode, false, problem.getNextTestCaseId(), isSampleTest);

        boolean success =  amazonClient.uploadFile(inputFileKey, inputFile) && amazonClient.uploadFile(outputFileKey, outputFile);
        if (!success) {
            return null;
        }

        TestCase testCase = new TestCase();
        testCase.setId(problem.getNextTestCaseId());
        testCase.setProblemCode(problemCode);
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

        problem.setNextTestCaseId(problem.getNextTestCaseId() + 1);
        problem.setNumberOfTestCases(problem.getNumberOfTestCases() + 1);
        problem.setUpdatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);

        if (isSampleTest) {
            return mapper.map(testCase1, SampleTestCaseDTO.class);
        }
        else {
            return mapper.map(testCase1, TestCaseDTO.class);
        }

    }

    @Override
    public Boolean deleteTestCase(String problemCode, int testCaseId, boolean isSampleTest) {
        // add problem related logic
        Problem problem = validateProblemCode(problemCode);
        String inputFileKey = getFileName(problemCode, true, testCaseId, isSampleTest);
        String outputFileKey = getFileName(problemCode, false, testCaseId, isSampleTest);

        // delete test cases from s3 and db
        boolean success =  amazonClient.deleteFile(inputFileKey) && amazonClient.deleteFile(outputFileKey);
        if (!success) {
            return false;
        }

        TestCase testCase = testCaseRepository.findByProblemAndId(problem, (long) testCaseId);
        testCaseRepository.delete(testCase);

        problem.setNumberOfTestCases(problem.getNumberOfTestCases() - 1);
        problem.setUpdatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        return true;
    }

    private Boolean deleteAllTestCases(String problemCode) {
        Problem problem = validateProblemCode(problemCode);
        // delete all test cases from s3 and entries from db
        boolean success = amazonClient.deleteDirectory(problemCode);
        if (!success) {
            return false;
        }
        testCaseRepository.deleteAll(problem.getTestCases());
        return true;
    }


    private String getFileName(String problemCode, boolean isInputFile, long testCaseNumber,
                               boolean isSampleTest) {
        String fileName = problemCode;
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
}
