package com.projects.onlinejudge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponseDTO extends RunResponse{

    private long id;
    private String userName;
    private String problemCode;
    
}
