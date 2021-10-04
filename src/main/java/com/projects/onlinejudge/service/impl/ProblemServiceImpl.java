package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.User;
import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.ProblemService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private ModelMapper mapper;

    @Override
    public boolean createProblem(ProblemDTO problemDTO) {
        User user = validateUser(problemDTO.getUserName());
        validateProblemCode(problemDTO.getProblemCode());
        Problem problem = mapper.map(problemDTO, Problem.class);
        problem.setUser(user);
        problem.setCreatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        return true;
    }

    @Override
    public ProblemDTO getProblem(String problemCode) {
        Problem problem = problemRepository.findProblemByProblemCode(problemCode);
        if (Objects.isNull(problem)) {
            // raise ProblemCodeDoesNotExistException
        }
        return mapper.map(problem, ProblemDTO.class);
    }

    @Override
    public boolean updateProblem(ProblemDTO problemDTO) {
        User user = validateUser(problemDTO.getUserName());
        validateProblemCode(problemDTO.getProblemCode());
        Problem problem = problemRepository.findProblemByProblemCode(problemDTO.getProblemCode());
        problem.setConstraints(problemDTO.getConstraints());
        problem.setDescription(problemDTO.getDescription());
        problem.setInputFormat(problemDTO.getInputFormat());
        problem.setOutputFormat(problemDTO.getOutputFormat());
        problem.setTitle(problemDTO.getTitle());
        problem.setUpdatedAt(Date.from(Instant.now()));
        problemRepository.save(problem);
        return true;
    }

    @Override
    public Boolean deleteProblem(String problemCode) {
        Problem problem = problemRepository.findProblemByProblemCode(problemCode);
        if (Objects.isNull(problem)) {
            // raise ProblemCodeDoesNotExistException
        }
        problemRepository.delete(problem);
        return true;
    }

    @Override
    public Boolean addTestCase(String problemCode, int testCaseNumber, MultipartFile[] multipartFiles) {
        // add problem related logic
        String inputFileKey = getFileName(problemCode, true, testCaseNumber);
        String outputFileKey = getFileName(problemCode, false, testCaseNumber);
        return amazonClient.uploadFile(inputFileKey, multipartFiles[0]) &&
                amazonClient.uploadFile(outputFileKey, multipartFiles[1]);
    }

    @Override
    public Boolean deleteTestCase(String problemCode, int testCaseNumber) {
        // add problem related logic
        String inputFileKey = getFileName(problemCode, true, testCaseNumber);
        String outputFileKey = getFileName(problemCode, false, testCaseNumber);
        return amazonClient.deleteFile(inputFileKey) && amazonClient.deleteFile(outputFileKey);
    }

    @Override
    public Boolean deleteAllTestCases(String problemCode) {
        return amazonClient.deleteDirectory(problemCode);
    }


    private String getFileName(String problemCode, boolean isInputFile, int testCaseNumber) {
        return problemCode.concat(isInputFile? FileConstants.INPUT_FILE: FileConstants.OUTPUT_FILE) + testCaseNumber +
                FileConstants.TEXT_FILE_EXT;
    }

    private User validateUser(String userName) {
        User user = userRepository.findUserByUserName(userName);
        if (Objects.isNull(user)) {
            // raise custom exception - UserNameDoesNotExistException
        }
        return user;
    }

    private void validateProblemCode(String problemCode) {
        Problem oldProblem = problemRepository.findProblemByProblemCode(problemCode);
        if (Objects.nonNull(oldProblem)) {
            // raise ProblemCodeAlreadyExists custom exception
        }
    }
}
