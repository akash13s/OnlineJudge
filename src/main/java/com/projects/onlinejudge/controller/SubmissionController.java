package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.RunRequest;
import com.projects.onlinejudge.dto.RunResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submit")
public class SubmissionController {


    @PostMapping("/run")
    public RunResponse validate(@RequestBody RunRequest attribute) throws Exception {
        Integer passed = 9;
        Integer failed = 0;
        int p = (int) (Math.random());
        passed= p;
        System.out.println("CodeUrl :" +attribute.getCodeUrl());
        System.out.println("User ID  :" +attribute.getUserId());

        System.out.println("Submission ID :" +attribute.getSubmissionId());

        System.out.println("User Language :" +attribute.getLang());

        RunResponse runResponse = new RunResponse(passed,failed);
        return runResponse;
    }
}
