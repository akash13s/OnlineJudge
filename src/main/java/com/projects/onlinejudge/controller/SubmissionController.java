package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.SubmissionResponseDTO;
import com.projects.onlinejudge.service.impl.SubmissionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;


@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private static final String ORDER_SERVICE ="orderService" ;

    @Autowired
    private SubmissionServiceImpl submissionService;

    @PostMapping("/create")
    @RateLimiter(name=ORDER_SERVICE, fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<?> createSubmission(@RequestParam("language") String language,
                                           @RequestParam("userName") String userName,
                                           @RequestParam("problemCode") String problemCode,
                                           @RequestParam("code") MultipartFile code) throws Exception {
        // Setting contestId as 0 to prevent NullPointException
        // Note: we could not use this as contest id for any contest
        SubmissionResponseDTO responseDTO = submissionService.submitCode(language, userName, problemCode, code, 0L);
        return new ResponseEntity<SubmissionResponseDTO>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/create-contest-submission")
    @RateLimiter(name=ORDER_SERVICE, fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<?> createContestSubmission(@RequestParam("language") String language,
                                              @RequestParam("userName") String userName,
                                              @RequestParam("problemCode") String problemCode,
                                              @RequestParam("code") MultipartFile code,
                                              @RequestParam("contestId") Long contestId) throws Exception {

        SubmissionResponseDTO responseDTO = submissionService.submitCode(language, userName, problemCode, code,
                contestId);
        return new ResponseEntity<SubmissionResponseDTO>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/download/{problemCode}")
    @RateLimiter(name=ORDER_SERVICE, fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<?> downloadProblem(@PathVariable("problemCode") String problemCode) {
        boolean success = submissionService.downloadProblem(problemCode);
        return new ResponseEntity<>(success? "problem successfully downloaded": "unable to download problem", HttpStatus.OK);
    }

    public ResponseEntity<String> rateLimiterFallback(Exception e) {
        return new ResponseEntity<String>("Submission Controller does not permit further calls", HttpStatus.TOO_MANY_REQUESTS);
    }

}
