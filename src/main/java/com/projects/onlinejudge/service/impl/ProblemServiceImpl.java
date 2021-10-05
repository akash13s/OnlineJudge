package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.SampleTestCase;
import com.projects.onlinejudge.domain.User;
import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.dto.SampleTestCaseDTO;
import com.projects.onlinejudge.dto.TestCaseDTO;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.repository.SampleTestCaseRepository;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.ProblemService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Struct;
import java.time.Instant;
import java.util.ArrayList;
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
    private SampleTestCaseRepository sampleTestCaseRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ProblemDTO createProblem(ProblemDTO problemDTO) {
        User user = validateUser(problemDTO.getUserName());
        Problem existingProblem = problemRepository.findProblemByProblemCode(problemDTO.getProblemCode());
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

        SampleTestCase sampleTestCase1 = null;

        if (isSampleTest) {
            SampleTestCase sampleTestCase = new SampleTestCase();
            sampleTestCase.setId(problem.getNextTestCaseId());
            // convert multipart file content to string
            sampleTestCase.setSampleInput(new String(inputFile.getBytes()));
            sampleTestCase.setSampleOutput(new String(outputFile.getBytes()));

            sampleTestCase.setProblem(problem);
            sampleTestCase.setProblemCode(problemCode);
            sampleTestCase1 = sampleTestCaseRepository.save(sampleTestCase);
        }

        problem.setNextTestCaseId(problem.getNextTestCaseId() + 1);
        problem.setNumberOfTestCases(problem.getNumberOfTestCases() + 1);
        problem.setUpdatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        if (isSampleTest) {
            SampleTestCaseDTO sampleTestCaseDTO1 =  mapper.map(sampleTestCase1, SampleTestCaseDTO.class);
            sampleTestCaseDTO1.setIsSampleTest(true);
            return  sampleTestCaseDTO1;
        }
        else {
            TestCaseDTO testCaseDTO = new TestCaseDTO();
            testCaseDTO.setId(problem.getNextTestCaseId() - 1);
            testCaseDTO.setProblemCode(problemCode);
            return testCaseDTO;
        }

    }

    @Override
    public Boolean deleteTestCase(String problemCode, int testCaseId, boolean isSampleTest) {
        // add problem related logic
        Problem problem = validateProblemCode(problemCode);
        String inputFileKey = getFileName(problemCode, true, testCaseId, isSampleTest);
        String outputFileKey = getFileName(problemCode, false, testCaseId, isSampleTest);
        boolean success =  amazonClient.deleteFile(inputFileKey) && amazonClient.deleteFile(outputFileKey);
        if (!success) {
            return false;
        }
        if (isSampleTest) {
            SampleTestCase sampleTestCase = sampleTestCaseRepository.findByProblemAndId(problem, (long) testCaseId);
            sampleTestCaseRepository.delete(sampleTestCase);
        }
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
        sampleTestCaseRepository.deleteAll(problem.getSampleTestCases());
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
