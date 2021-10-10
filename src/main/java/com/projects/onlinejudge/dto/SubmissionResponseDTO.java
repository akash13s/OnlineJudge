package com.projects.onlinejudge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponseDTO {

    private Long id;
    private String verdict;
    private Long testCasesPassed;
    private Long testCasesFailed;
    private String userName;
    private String problemCode;
    
}
