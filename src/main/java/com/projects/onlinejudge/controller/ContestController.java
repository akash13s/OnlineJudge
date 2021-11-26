package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.ContestDTO;
import com.projects.onlinejudge.dto.ContestProblemDTO;
import com.projects.onlinejudge.service.impl.ContestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/contests")
public class ContestController {

    @Autowired
    private ContestServiceImpl contestService;

    @PostMapping("/create")
    public ResponseEntity<ContestDTO> createContest(@RequestBody ContestDTO contestDTO) {
        return new ResponseEntity<>(contestService.createContest(contestDTO), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ContestDTO> updateContest(@RequestBody ContestDTO contestDTO) {
        return new ResponseEntity<>(contestService.updateContest(contestDTO), HttpStatus.OK);
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<ContestDTO> fetchContest(@PathVariable("contestId") Long contestId) {
        return new ResponseEntity<>(contestService.getContest(contestId), HttpStatus.OK);
    }

    @DeleteMapping("/{contestId}")
    public ResponseEntity<String> deleteContest(@PathVariable("contestId") Long contestId) {
        contestService.deleteContest(contestId);
        return new ResponseEntity<>("Contest successfully deleted", HttpStatus.OK);
    }

    @PostMapping("/add-problem")
    public ResponseEntity<String> addProblemToContest(@RequestBody ContestProblemDTO contestProblemDTO) {
        boolean success = contestService.addProblemToContest(contestProblemDTO.getContestId(), contestProblemDTO.getProblemCode());
        if (success) {
            return new ResponseEntity<String>("Problem added successfully to contest", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("Problem could not be added to contest", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/remove-problem")
    public ResponseEntity<String> removeProblemFromContest(@RequestBody ContestProblemDTO contestProblemDTO) {
        boolean success = contestService.removeProblemFromContest(contestProblemDTO.getContestId(), contestProblemDTO.getProblemCode());
        if (success) {
            return new ResponseEntity<String>("Problem removed successfully from contest", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("Problem could not be removed from contest", HttpStatus.BAD_REQUEST);
        }
    }
}
