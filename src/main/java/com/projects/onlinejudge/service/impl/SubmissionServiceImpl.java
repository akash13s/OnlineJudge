package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.config.LanguageConfig;
import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.core.Runner;
import com.projects.onlinejudge.domain.Contest;
import com.projects.onlinejudge.domain.Submission;
import com.projects.onlinejudge.dto.RunRequest;
import com.projects.onlinejudge.dto.RunResponse;
import com.projects.onlinejudge.dto.SubmissionResponseDTO;
import com.projects.onlinejudge.repository.ContestRepository;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.repository.SubmissionRepository;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.SubmissionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Null;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LanguageConfig languageConfig;

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private Runner runner;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private ContestRepository contestRepository;

    @Override
    public SubmissionResponseDTO submitCode(String language, String userName,
                                            String problemCode, MultipartFile code, Long contestId) {

        // validate language, username, problemCode
        validateSubmissionFields(language, userName, problemCode, code, contestId);

        Submission submission = new Submission();
        submission.setProblemCode(problemCode);
        submission.setUserName(userName);
        submission.setLanguage(language);
        submission.setCreatedAt(Date.from(Instant.now()));
        submission.setContestId(contestId);
        submissionRepository.save(submission);

        // save submission in s3
        String awsSubmissionKey = getSubmissionKey(userName, submission.getId());
        amazonClient.uploadFile(awsSubmissionKey, code);

        // set awsSubmissionKey and save in db
        submission.setAwsSubmissionKey(awsSubmissionKey);
        submissionRepository.save(submission);

        /*
            We are calling runner directly here. Later on, we need to call kafka producer from here.
        */
        RunRequest runRequest = mapper.map(submission, RunRequest.class);
        runRequest.setFileName(getFilename(Objects.requireNonNull(code.getOriginalFilename())));
        RunResponse runResponse = runner.runTests(runRequest);

        // save submission verdict
        submission.setVerdict(runResponse.getVerdict());
        submission.setMessage(runResponse.getMessage());
        submission.setTestCasesCount(runResponse.getTestCasesCount());
        submission.setPassedCount(runResponse.getPassedCount());
        submission.setWrongAnswerCount(runResponse.getWrongAnswerCount());
        submission.setTimeLimitExceededCount(runResponse.getTimeLimitExceededCount());
        submissionRepository.save(submission);

        // return response
        SubmissionResponseDTO responseDTO = mapper.map(runResponse, SubmissionResponseDTO.class);
        responseDTO.setUserName(userName);
        responseDTO.setProblemCode(problemCode);

        return responseDTO;
    }

    private String getFilename(String originalFilename) {
        if (originalFilename.contains(".")) {
            int idx = originalFilename.indexOf('.');
            return originalFilename.substring(0, idx);
        }
        else {
            return originalFilename;
        }
    }

    @Override
    public Boolean downloadProblem(String problemCode) {
        amazonClient.downloadDirectory(FileConstants.PROBLEMS + "/" + problemCode, runner.getProblemTestCasesDirectory());
        return true;
    }


    private String getSubmissionKey(String userName, Long id) {
        return FileConstants.SUBMISSIONS + "/" + userName + "/" + id + FileConstants.TEXT_FILE_EXT;
    }

    private void validateSubmissionFields(String language, String userName,
                                          String problemCode, MultipartFile code, Long contestId) {

        if (!languageConfig.getLanguageMap().containsKey(language)) {
            // raise LanguageNotSupportedException
        }
        if (userRepository.findUserByUserName(userName) == null) {
            // raise UserNameDoesNotExistException
        }
        if (problemRepository.findProblemByProblemCode(problemCode) == null) {
            // raise ProblemCodeDoesNotExistException
        }
        if (contestId != null) {
            Optional<Contest> optionalContest = contestRepository.findById(contestId);
            if(!optionalContest.isPresent()) {
                // raise ContestDoesNotExistException
            }
            else {
                Contest contest = optionalContest.get();
                Date currentDate = Date.from(Instant.now());
                if(currentDate.before(contest.getStartDate())) {
                    // raise SubmissionBeforeContestTime
                }
                if(currentDate.after((contest.getEndDate()))){
                    // raise SubmissionAfterContestTime
                }
            }

        }
        // we can add checks related to code as well later on
    }
}
