package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.RunRequest;
import com.projects.onlinejudge.dto.RunResponse;
import com.projects.onlinejudge.helpers.IRunner;
import com.projects.onlinejudge.helpers.LanguageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/api/submit")
public class SubmissionController {

    @Autowired
    LanguageFactory languageFactory;
    @PostMapping("/run")
    public RunResponse validate(@RequestBody RunRequest attribute) throws Exception {

        System.out.println("CodeUrl :" +attribute.getCodeUrl());
        System.out.println("User ID  :" +attribute.getUserId());
        System.out.println("Submission ID :" +attribute.getSubmissionId());
        System.out.println("User Language :" +attribute.getLang());
        String lang= attribute.getLang();
        ArrayList<String> languagesAvailable = new ArrayList<>(Arrays.asList("cpp", "java", "python"));
        Assert.isTrue (languagesAvailable.contains(lang), "Wrong Language Provided. Please try from any of the following [cpp, java, python]\n"); // : "Wrong Language Provided. Please try from any of the following [cpp, java, python]";

        IRunner runnerFactory = languageFactory.getLangRunner(lang);
        RunResponse runResponse = runnerFactory.runTests(attribute);
        return runResponse;
    }
}
