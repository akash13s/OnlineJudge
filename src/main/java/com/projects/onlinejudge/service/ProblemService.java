package com.projects.onlinejudge.service;

import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.dto.TestCaseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProblemService {

    public ProblemDTO createProblem(ProblemDTO problemDTO);
    public ProblemDTO getProblem(String problemCode);
    public ProblemDTO updateProblem(ProblemDTO problemDTO);
    public Boolean deleteProblem(String problemCode);
    public TestCaseDTO addTestCase(String problemCode, MultipartFile inputFile, MultipartFile outputFile, boolean isSampleTest) throws IOException;
    public Boolean deleteTestCase(String problemCode, int testCaseId, boolean isSampleTest) throws IOException;
}
