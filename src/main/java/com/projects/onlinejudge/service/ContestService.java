package com.projects.onlinejudge.service;

import com.projects.onlinejudge.dto.ContestDTO;

public interface ContestService {

    ContestDTO createContest(ContestDTO contestDTO);
    ContestDTO updateContest(ContestDTO contestDTO);
    ContestDTO getContest(Long id);
    void deleteContest(Long id);
    boolean addProblemToContest(Long contestId, String problemCode);
    boolean removeProblemFromContest(Long contestId, String problemCode);
    void registerForContest(Long contestId, String userName);
    void unregisterForContest(Long contestId, String userName);
}
