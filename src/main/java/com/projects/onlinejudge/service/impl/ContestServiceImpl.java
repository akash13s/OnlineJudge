package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.domain.Contest;
import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.User;
import com.projects.onlinejudge.dto.ContestDTO;
import com.projects.onlinejudge.repository.ContestRepository;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.ContestService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class ContestServiceImpl implements ContestService {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

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
        Contest contest = validateContest(contestDTO.getId());
        if (Objects.isNull(contest)) {
            // raise contest does not exist exception
        }
        contest.setContestName(contestDTO.getContestName());
        contest.setDescription(contestDTO.getDescription());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setEndDate(contestDTO.getEndDate());
        contest.setRules(contestDTO.getRules());
        contest.setScoring(contestDTO.getScoring());
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
        Problem problem = validateProblem(problemCode);
        if (Objects.isNull(contest) || Objects.isNull(problem)) {
            return false;
        }
        problem.setContest(contest);
        problemRepository.save(problem);
        return true;
    }

    @Override
    public boolean removeProblemFromContest(Long contestId, String problemCode) {
        Contest contest = validateContest(contestId);
        Problem problem = validateProblem(problemCode);
        if (Objects.isNull(contest) || Objects.isNull(problem)) {
            return false;
        }
        contest.getProblems().remove(problem);
        contestRepository.save(contest);
        problem.setContest(null);
        problemRepository.save(problem);
        return true;
    }

    @Override
    public void registerForContest(Long contestId, String userName) {
        Contest contest = validateContest(contestId);
        User user = userRepository.findUserByUserName(userName);
        if (contest!=null && user!=null) {
            contest.getParticipants().add(user);
        }
    }

    @Override
    public void unregisterForContest(Long contestId, String userName) {
        Contest contest = validateContest(contestId);
        User user = userRepository.findUserByUserName(userName);
        if (contest!=null && user!=null) {
            contest.getParticipants().remove(user);
        }
    }

    private Contest validateContest(Long contestId) {
        Optional<Contest> contest = contestRepository.findById(contestId);
        return contest.orElse(null);
    }

    private Problem validateProblem(String problemCode) {
        Problem problem = problemRepository.findProblemByProblemCode(problemCode);
        if (Objects.nonNull(problem)) {
            return problem;
        }
        return null;
    }
}
