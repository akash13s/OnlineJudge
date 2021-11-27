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

import java.util.ArrayList;
import java.util.List;
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
    public boolean deleteContest(Long id) {
        Contest contest = validateContest(id);
        if (Objects.isNull(contest)) {
            return false;
        }
        contestRepository.delete(contest);
        return true;
    }

    @Override
    public boolean addProblemToContest(Long contestId, String problemCode) {
        Contest contest = validateContest(contestId);
        Problem problem = validateProblem(problemCode);
        if (Objects.isNull(contest) || Objects.isNull(problem)) {
            return false;
        }
        // check if we are not adding the same problem again to the contest
        List<String> contestProblemNames = new ArrayList<>();
        contest.getProblems().forEach(contestProblem -> contestProblemNames.add(contestProblem.getProblemCode()));

        if (contestProblemNames.contains(problemCode)) {
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
    public boolean registerParticipantForContest(String userName, Long contestId) {
        User user = validateUser(userName);
        Contest contest = validateContest(contestId);
        if (user!=null && contest!=null) {
            // check if the same user has not been registered multiple  times
            List<String> participants = getContestParticipants(contestId);
            if (participants.contains(userName)) {
                return false;
            }

            user.getContests().add(contest);
            userRepository.save(user);
            contest.getParticipants().add(user);
            contestRepository.save(contest);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregisterParticipantFromContest(String userName, Long contestId) {
        User user = validateUser(userName);
        Contest contest = validateContest(contestId);
        if (user!=null && contest!=null) {
            user.getContests().remove(contest);
            userRepository.save(user);
            contest.getParticipants().remove(user);
            contestRepository.save(contest);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getContestParticipants(Long contestId) {
        List<String> participants = new ArrayList<>();
        Contest contest = validateContest(contestId);
        if (Objects.nonNull(contest)) {
            contest.getParticipants().forEach(user -> participants.add(user.getUserName()));
        }
        return participants;
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

    private User validateUser(String userName) {
        User user = userRepository.findUserByUserName(userName);
        if (Objects.nonNull(user)) {
            return user;
        }
        return null;
    }

}
