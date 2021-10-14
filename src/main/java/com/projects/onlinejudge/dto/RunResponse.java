package com.projects.onlinejudge.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RunResponse implements Serializable {

    private long id;
    String verdict;
    String message;
    long testCasesCount;
    long passedCount;
    long wrongAnswerCount;
    long timeLimitExceededCount;
//    long memoryLimitExceededCount;


    public RunResponse(long id, String verdict, String message, long testCasesCount,
                       long passedCount, long wrongAnswerCount, long timeLimitExceededCount) {
        this.id = id;
        this.verdict = verdict;
        this.message = message;
        this.testCasesCount = testCasesCount;
        this.passedCount = passedCount;
        this.wrongAnswerCount = wrongAnswerCount;
        this.timeLimitExceededCount = timeLimitExceededCount;
    }

    public RunResponse() {
    }
}
