package com.projects.onlinejudge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunRequest implements Serializable {
    private String language;
    private String awsSubmissionKey;
    private String userName;
    private String submissionId;
    private String problemCode;
    private String fileName;
    private long contestId;
}
