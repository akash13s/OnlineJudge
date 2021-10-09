package com.projects.onlinejudge.service.impl;

import com.projects.onlinejudge.config.LanguageConfig;
import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.core.Runner;
import com.projects.onlinejudge.domain.Submission;
import com.projects.onlinejudge.dto.RunRequest;
import com.projects.onlinejudge.dto.RunResponse;
import com.projects.onlinejudge.dto.SubmissionResponseDTO;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.repository.SubmissionRepository;
import com.projects.onlinejudge.repository.UserRepository;
import com.projects.onlinejudge.service.SubmissionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

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

    @Override
    public SubmissionResponseDTO submitCode(String language, String userName,
                                            String problemCode, MultipartFile code) {

        // validate language, username, problemCode
        validateSubmissionFields(language, userName, problemCode, code);

        Submission submission = new Submission();
        submission.setProblemCode(problemCode);
        submission.setUserName(userName);
        submission.setLanguage(language);
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
        SubmissionResponseDTO responseDTO = new SubmissionResponseDTO();
        responseDTO.setId(submission.getId());
        responseDTO.setProblemCode(problemCode);
        responseDTO.setUserName(userName);
        responseDTO.setVerdict(responseDTO.getVerdict());
        responseDTO.setTestCasesFailed(runResponse.getFailed());
        responseDTO.setTestCasesPassed(runResponse.getPassed());
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
        amazonClient.downloadDirectory(problemCode, runner.getProblemTestCasesDirectory());
        return true;
    }


    private String getSubmissionKey(String userName, Long id) {
        return userName + "/" + id + FileConstants.TEXT_FILE_EXT;
    }

    private void validateSubmissionFields(String language, String userName,
                                          String problemCode, MultipartFile code) {

        if (!languageConfig.getLanguageMap().containsKey(language)) {
            // raise LanguageNotSupportedException
        }
        if (userRepository.findUserByUserName(userName) == null) {
            // raise UserNameDoesNotExistException
        }
        if (problemRepository.findProblemByProblemCode(problemCode) == null) {
            // raise ProblemCodeDoesNotExistException
        }
        // we can add checks related to code as well later on
    }
}
