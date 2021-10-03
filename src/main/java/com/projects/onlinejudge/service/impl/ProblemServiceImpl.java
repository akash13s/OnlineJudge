package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private AmazonClient amazonClient;

    @Override
    public ProblemDTO createProblem(ProblemDTO problemDTO) {
        return null;
    }

    @Override
    public ProblemDTO getProblem(String problemCode) {
        return null;
    }

    @Override
    public ProblemDTO updateProblem(ProblemDTO problemDTO) {
        return null;
    }

    @Override
    public Boolean deleteProblem(String problemCode) {
        return null;
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
}
