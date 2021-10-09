package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.SubmissionResponseDTO;
import com.projects.onlinejudge.service.impl.SubmissionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionServiceImpl submissionService;

    @PostMapping("/create")
    public ResponseEntity<?> createSubmission(@RequestParam("language") String language,
                                           @RequestParam("userName") String userName,
                                           @RequestParam("problemCode") String problemCode,
                                           @RequestParam("code") MultipartFile code) throws Exception {

        SubmissionResponseDTO responseDTO = submissionService.submitCode(language, userName, problemCode, code);
        return new ResponseEntity<SubmissionResponseDTO>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/download/{problemCode}")
    public ResponseEntity<?> downloadProblem(@PathVariable("problemCode") String problemCode) {
        boolean success = submissionService.downloadProblem(problemCode);
        return new ResponseEntity<>(success? "problem successfully downloaded": "unable to download problem", HttpStatus.OK);
    }
}
