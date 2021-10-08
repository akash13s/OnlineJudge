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
    String language;
    String awsSubmissionKey;
    String userName;
    String submissionId;
    String problemCode;
}
