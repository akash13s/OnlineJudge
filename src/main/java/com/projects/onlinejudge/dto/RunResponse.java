package com.projects.onlinejudge.dto;

import com.sun.tools.javac.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public class RunResponse implements Serializable {
    Integer passed;
    Integer failed;
    ArrayList<Pair<Integer,String>>  testResults;

    public RunResponse(Integer passed, Integer failed, ArrayList<Pair<Integer, String>> testResults) {
        this.passed = passed;
        this.failed = failed;
        this.testResults = testResults;
    }

    public RunResponse(Integer passedCount, Integer failedCount) {
        this.passed = passed;
        this.failed = failed;

    }

    public ArrayList<Pair<Integer, String>> getTestResults() {
        return testResults;
    }

    public void setTestResults(ArrayList<Pair<Integer, String>> testResults) {
        this.testResults = testResults;
    }

    public Integer getPassed() {
        return passed;
    }

    public void setPassed(Integer passed) {
        this.passed = passed;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }
}
