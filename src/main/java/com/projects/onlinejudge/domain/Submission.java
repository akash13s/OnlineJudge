package com.projects.onlinejudge.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String language;
    private String awsSubmissionKey;
    private String userName;
    private String problemCode;
    private String verdict;
    private String message;
    private long testCasesCount;
    private long passedCount;
    private long wrongAnswerCount;
    private long timeLimitExceededCount;
    private Date createdAt;
    private Long contestId;
}
