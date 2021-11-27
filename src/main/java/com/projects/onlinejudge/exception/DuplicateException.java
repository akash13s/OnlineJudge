package com.projects.onlinejudge.exception;

public class DuplicateException extends Exception {
    public String message;
    public DuplicateException(String message)
    {
        this.message = message;
    }
}
