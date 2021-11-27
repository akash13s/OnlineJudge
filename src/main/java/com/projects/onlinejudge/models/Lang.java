package com.projects.onlinejudge.models;

public enum Lang {
    CPP("cpp"),
    JAVA("java"),
    PYTHON("python"),
    C("c");

    String value;
    Lang(String lang) {
        this.value= lang;
    }
    public String getValue() {
        return value;
    }
}
