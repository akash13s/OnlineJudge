package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.domain.Contest;
import com.projects.onlinejudge.dto.ContestDTO;
import com.projects.onlinejudge.repository.ContestRepository;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.service.ContestService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;

public class ContestServiceImpl implements ContestService {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ContestDTO createContest(ContestDTO contestDTO) {

        Contest existingContest = contestRepository.findByContestName(contestDTO.getContestName());
        if (Objects.nonNull(existingContest)) {
            // raise contest name already exists exception
        }
        Contest contest = mapper.map(contestDTO, Contest.class);
        contestRepository.save(contest);
        return mapper.map(contest, ContestDTO.class);
    }

    @Override
    public ContestDTO updateContest(ContestDTO contestDTO) {
        Contest contest = contestRepository.findByContestName(contestDTO.getContestName());
        if (Objects.isNull(contest)) {
            // raise contest does not exist exception
        }
        contest.setContestName(contestDTO.getContestName());
        contest.setDescription(contest.getDescription());
        contest.setStartDate(contest.getStartDate());
        contest.setEndDate(contest.getEndDate());
        contest.setRules(contest.getRules());
        contest.setScoring(contest.getScoring());
        contestRepository.save(contest);
        return mapper.map(contest, ContestDTO.class);
    }

    @Override
    public ContestDTO getContest(Long id) {
        Optional<Contest> optionalContest = contestRepository.findById(id);
        return optionalContest.map(contest -> mapper.map(contest, ContestDTO.class)).orElse(null);
    }

    @Override
    public void deleteContest(Long id) {
        contestRepository.deleteById(id);
    }

    @Override
    public boolean addProblemToContest(Long contestId, String problemCode) {
        Contest contest = validateContest(contestId);
        if (Objects.isNull(contest)) {
            return false;
        }
        contest.getProblems().add(problemRepository.findProblemByProblemCode(problemCode));
        contestRepository.save(contest);
        return true;
    }

    @Override
    public boolean removeProblemFromContest(Long contestId, String problemCode) {
        Contest contest = validateContest(contestId);
        if (Objects.isNull(contest)) {
            return false;
        }
        contest.getProblems().remove(problemRepository.findProblemByProblemCode(problemCode));
        contestRepository.save(contest);
        return false;
    }

    private Contest validateContest(Long contestId) {
        Optional<Contest> contest = contestRepository.findById(contestId);
        return contest.orElse(null);
    }
}
