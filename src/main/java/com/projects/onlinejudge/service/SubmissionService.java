package com.projects.onlinejudge.service;

import com.projects.onlinejudge.dto.SubmissionResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {

    public SubmissionResponseDTO submitCode(String language, String userName, String problemCode,
                                            MultipartFile code);

    public Boolean downloadProblem(String problemCode);
}
