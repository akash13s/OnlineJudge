package com.projects.onlinejudge.service;

import com.projects.onlinejudge.dto.ProblemDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProblemService {

    public ProblemDTO createProblem(ProblemDTO problemDTO);
    public ProblemDTO getProblem(String problemCode);
    public ProblemDTO updateProblem(ProblemDTO problemDTO);
    public Boolean deleteProblem(String problemCode);
    public Boolean addTestCase(String problemCode, int testCaseNumber, MultipartFile[] multipartFiles);
    public Boolean deleteTestCase(String problemCode, int testCaseNumber);
    public Boolean deleteAllTestCases(String problemCode);
}
