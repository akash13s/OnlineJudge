package com.projects.onlinejudge.service;

import com.projects.onlinejudge.dto.ContestDTO;

import java.util.List;

public interface ContestService {

    ContestDTO createContest(ContestDTO contestDTO);
    ContestDTO updateContest(ContestDTO contestDTO);
    ContestDTO getContest(Long id);
    boolean deleteContest(Long id);
    boolean addProblemToContest(Long contestId, String problemCode);
    boolean removeProblemFromContest(Long contestId, String problemCode);
    boolean registerParticipantForContest(String userName, Long contestId);
    boolean unregisterParticipantFromContest(String userName, Long contestId);
    List<String> getContestParticipants(Long contestId);
}
