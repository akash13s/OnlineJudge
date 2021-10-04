package com.projects.onlinejudge.controller;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.service.impl.ProblemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.Response;


@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemServiceImpl problemService;

    @PostMapping("/create")
    public ResponseEntity<?> createProblem(@RequestBody ProblemDTO problemDTO) {
        boolean success = problemService.createProblem(problemDTO);
        return getResponse(success, "Problem created successfully",
                "Unable to create problem");
    }

    @GetMapping("/{problemCode}")
    public ResponseEntity<?> getProblem(@PathVariable("problemCode") String problemCode) {
        ProblemDTO problemDTO = problemService.getProblem(problemCode);
        return new ResponseEntity<>(problemDTO, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProblem(@RequestBody ProblemDTO problemDTO) {
        boolean success = problemService.updateProblem(problemDTO);
        return getResponse(success, "Problem updated successfully", "Unable to update problem");
    }

    @DeleteMapping("/remove/{problemCode}")
    public ResponseEntity<?> removeProblem(@PathVariable("problemCode") String problemCode) {
        boolean success = problemService.deleteProblem(problemCode);
        return getResponse(success, "Problem removed successfully", "Unable to remove problem");
    }

    @PostMapping("/add/testcase/{problemCode}/{testCaseNumber}")
    public ResponseEntity<?> addProblemTestCase(@PathVariable("problemCode") String problemCode,
                                                @PathVariable("testCaseNumber") Integer testCaseNumber,
                                                @RequestParam("files") MultipartFile[] files) {

        boolean success = problemService.addTestCase(problemCode, testCaseNumber, files);
        return getResponse(success, "Test case added successfully", "Failed to add test case");
    }

    @DeleteMapping("/remove/testcase/{problemCode}/{testCaseNumber}")
    public ResponseEntity<?> removeTestCase(@PathVariable("problemCode") String problemCode,
                                            @PathVariable("testCaseNumber") Integer testCaseNumber) {
        boolean success = problemService.deleteTestCase(problemCode, testCaseNumber);
        return getResponse(success, "Test case removed successfully", "Failed to remove test case");
    }

    @DeleteMapping("/remove/testcases/{problemCode}")
    public ResponseEntity<?> removeAllTestCases(@PathVariable("problemCode") String problemCode) {
        boolean success = problemService.deleteAllTestCases(problemCode);
        return getResponse(success, "All test case removed successfully", "Failed to remove test cases");
    }

    private ResponseEntity<String> getResponse(boolean success, String successMessage, String failureMessage) {
        if (success) {
            return new ResponseEntity<>(successMessage, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(failureMessage, HttpStatus.BAD_REQUEST);
        }
    }
}
