package com.projects.onlinejudge.dto;

import java.io.Serializable;

public class RunRequest implements Serializable {
    String lang;
    String codeUrl;

    public RunRequest(String lang, String codeUrl, String userId, String submissionId,String qname) {
        this.lang = lang;
        this.codeUrl = codeUrl;
        this.userId = userId;
        this.submissionId = submissionId;
        this.qname=qname;
    }

    String userId;
    String submissionId;
    String qname;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    public String getQname() {
        return qname;
    }
}
