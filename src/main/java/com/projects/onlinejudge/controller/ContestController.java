package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.ContestDTO;
import com.projects.onlinejudge.dto.ContestProblemDTO;
import com.projects.onlinejudge.dto.ContestRegistrationDTO;
import com.projects.onlinejudge.service.impl.ContestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/remove-problem")
    public ResponseEntity<String> removeProblemFromContest(@RequestBody ContestProblemDTO contestProblemDTO) {
        boolean success = contestService.removeProblemFromContest(contestProblemDTO.getContestId(), contestProblemDTO.getProblemCode());
        if (success) {
            return new ResponseEntity<String>("Problem removed successfully from contest", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("Problem could not be removed from contest", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerParticipantForContest(@RequestBody ContestRegistrationDTO contestRegistrationDTO) {
        boolean success = contestService.registerParticipantForContest(contestRegistrationDTO.getUserName(),
                contestRegistrationDTO.getContestId());
        if (success) {
            return new ResponseEntity<String>("User successfully registered to contest", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("User could not be registered for contest", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<String> unregisterParticipantFromContest(@RequestBody ContestRegistrationDTO contestRegistrationDTO) {
        boolean success = contestService.unregisterParticipantFromContest(contestRegistrationDTO.getUserName(),
                contestRegistrationDTO.getContestId());
        if (success) {
            return new ResponseEntity<String>("User successfully unregistered from contest", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("User could not be unregistered from contest", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{contestId}/participants")
    public ResponseEntity<List<String>> getContestParticipants(@PathVariable("contestId") Long contestId) {
        List<String> participants = contestService.getContestParticipants(contestId);
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }

}
