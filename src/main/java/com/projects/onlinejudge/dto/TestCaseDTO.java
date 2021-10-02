package com.projects.onlinejudge.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestCaseDTO {

    private String problemCode;
    private Integer testCaseNumber;

    public String getProblemCode() {
        return problemCode;
    }

    public void setProblemCode(String problemCode) {
        this.problemCode = problemCode;
    }

    public Integer getTestCaseNumber() {
        return testCaseNumber;
    }

    public void setTestCaseNumber(Integer testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
    }
}
