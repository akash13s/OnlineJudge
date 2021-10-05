package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.ProblemDTO;
import com.projects.onlinejudge.dto.TestCaseDTO;
import com.projects.onlinejudge.service.impl.ProblemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;


@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemServiceImpl problemService;

    @PostMapping("/create")
    public ResponseEntity<?> createProblem(@RequestBody ProblemDTO problemDTO) {
        ProblemDTO problemDTO1 = problemService.createProblem(problemDTO);
        if (Objects.nonNull(problemDTO1)) {
            return new ResponseEntity<ProblemDTO>(problemDTO1, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("Unable to create problem", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{problemCode}")
    public ResponseEntity<?> getProblem(@PathVariable("problemCode") String problemCode) {
        ProblemDTO problemDTO = problemService.getProblem(problemCode);
        return new ResponseEntity<>(problemDTO, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProblem(@RequestBody ProblemDTO problemDTO) {
        ProblemDTO problemDTO1 = problemService.updateProblem(problemDTO);
        if (Objects.nonNull(problemDTO1)) {
            return new ResponseEntity<ProblemDTO>(problemDTO1, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("Unable to update problem", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/remove/{problemCode}")
    public ResponseEntity<?> removeProblem(@PathVariable("problemCode") String problemCode) {
        boolean success = problemService.deleteProblem(problemCode);
        return getResponse(success, "Problem removed successfully", "Unable to remove problem");
    }

    @PostMapping("/add/testcase/{problemCode}")
    public ResponseEntity<?> addProblemTestCase(@PathVariable("problemCode") String problemCode,
                                                @RequestParam("inputFile") MultipartFile inputFile,
                                                @RequestParam("outputFile") MultipartFile outputFile,
                                                @RequestParam("isSampleTest") Boolean isSampleTest) throws IOException {

        TestCaseDTO testCaseDTO = problemService.addTestCase(problemCode, inputFile, outputFile, isSampleTest);
        if (Objects.nonNull(testCaseDTO)) {
            return new ResponseEntity<TestCaseDTO>(testCaseDTO, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("Unable to add test case", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/remove/testcase")
    public ResponseEntity<?> removeTestCase(@RequestBody TestCaseDTO testCaseDTO) {
        boolean success = problemService.deleteTestCase(testCaseDTO.getProblemCode(), (int) testCaseDTO.getId(),
                testCaseDTO.getIsSampleTest());
        return getResponse(success, "Test case removed successfully", "Failed to remove test case");
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
