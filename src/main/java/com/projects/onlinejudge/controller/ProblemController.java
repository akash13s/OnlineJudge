package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.service.impl.ProblemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemServiceImpl problemService;

    @PostMapping("/add/testcase/{problemCode}/{testCaseNumber}")
    public ResponseEntity<?> addProblemTestCase(@PathVariable("problemCode") String problemCode,
                                                @PathVariable("testCaseNumber") Integer testCaseNumber,
                                                @RequestParam("files") MultipartFile[] files) {

        boolean success = problemService.addTestCase(problemCode, testCaseNumber, files);
        return new ResponseEntity<String>(success? "Test case added successfully": "Failed to add test case", HttpStatus.OK);
    }

    @DeleteMapping("/remove/testcase/{problemCode}/{testCaseNumber}")
    public ResponseEntity<?> removeTestCase(@PathVariable("problemCode") String problemCode,
                                            @PathVariable("testCaseNumber") Integer testCaseNumber) {
        boolean success = problemService.deleteTestCase(problemCode, testCaseNumber);
        return new ResponseEntity<String>(success? "Test case removed successfully": "Failed to remove test case", HttpStatus.OK);
    }

    @DeleteMapping("/remove/testcases/{problemCode}")
    public ResponseEntity<?> removeAllTestCases(@PathVariable("problemCode") String problemCode) {
        boolean success = problemService.deleteAllTestCases(problemCode);
        return new ResponseEntity<String>(success? "All test case removed successfully": "Failed to remove test cases", HttpStatus.OK);
    }
}
