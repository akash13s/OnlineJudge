package com.projects.onlinejudge.core;

import com.projects.onlinejudge.config.LanguageConfig;
import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.constants.RunnerConstants;
import com.projects.onlinejudge.constants.SubmissionConstants;
import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.TestCase;
import com.projects.onlinejudge.dto.RunRequest;
import com.projects.onlinejudge.dto.RunResponse;
import com.projects.onlinejudge.repository.ProblemRepository;
import com.projects.onlinejudge.service.impl.AmazonClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Runner extends RunnerUtils {

    @Value("${submissions.directory}")
    private String submissionsDirectory;

    @Value("${problem.testcases.directory}")
    private String problemTestCasesDirectory;

    @Value("${user.output.directory}")
    private String userOutputDirectory;

    public String getProblemTestCasesDirectory() {
        return problemTestCasesDirectory;
    }

    @Autowired
    private LanguageConfig languageConfig;

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private ProblemRepository problemRepository;

    public RunResponse runTests(RunRequest attribute)  {
        AtomicLong passedCount = new AtomicLong(0);
        AtomicLong wrongAnswerCount = new AtomicLong(0);
        AtomicLong timeLimitExceededCount = new AtomicLong(0);
        try {

            String lang = attribute.getLanguage();
            String userName = attribute.getUserName();
            String submissionId = attribute.getSubmissionId();
            String problemCode = attribute.getProblemCode();

            /*
            - For judging submission, check if problem test case directory is already present in the local file system.
                - If yes
                    - Check if the lastModified time for local problem test case directory>= lastModified time for s3 problem test case directory
                        - If yes, go ahead and use it
                        - Otherwise, download the new problem directory and overwrite the older version in LFS
                - Otherwise download the test case directory on the local file system.
             */
            Problem problem = problemRepository.findProblemByProblemCode(problemCode);
            String problemTestCaseDirectory = problemTestCasesDirectory + FileConstants.PROBLEMS + "/" + problemCode;

            if (Files.exists(Path.of(problemTestCaseDirectory))) {
                Date testCaseLastUpdatedAt = problem.getTestCaseLastUpdatedAt();
                File dir = new File(problemTestCaseDirectory);
                long localProblemTestCaseDirectoryLastUpdatedAt = getLastModifiedDateForProblemTestCaseFolder(problemTestCaseDirectory);
                if (testCaseLastUpdatedAt.after(Date.from(Instant.ofEpochMilli(localProblemTestCaseDirectoryLastUpdatedAt)))) {
                    // download updated test case directory from s3
                    FileUtils.deleteDirectory(dir);
                    amazonClient.downloadDirectory(FileConstants.PROBLEMS + "/" + problem.getProblemCode(), problemTestCasesDirectory);
                }
            }
            else {
                amazonClient.downloadDirectory(FileConstants.PROBLEMS + "/" + problem.getProblemCode(), problemTestCasesDirectory);
            }

            LanguageConfig.Language language = languageConfig.getLanguageMap().get(lang);

            /*
                Download user submission.
                User submission file path on local: {submissionsDirectory}/{submissionId}/{submissionId.(java/c++/py/....)}
             */
            String submissionDestinationFilePath = submissionsDirectory.concat(submissionId);
            File submissionDir = new File(submissionDestinationFilePath);
            if (!submissionDir.exists()) {
                submissionDir.mkdirs();
            }
            File tempFile = new File(submissionDir.getAbsolutePath() + "/" + submissionId + FileConstants.TEXT_FILE_EXT);
            tempFile.createNewFile();
            amazonClient.downloadFile(attribute.getAwsSubmissionKey(), tempFile.getAbsolutePath());

            // copy from txt file to program specific file
            File sourceFile = new File(submissionDir.getAbsolutePath() + "/" + submissionId + language.getFileExtension());
            copyContent(tempFile, sourceFile);
            tempFile.delete();

            // create command map
            Map<String, String> commandMap = new HashMap<>();
            commandMap.put(RunnerConstants.SOURCE_FILE_DIRECTORY, submissionDir.getAbsolutePath());
            commandMap.put(RunnerConstants.SUBMISSION_ID, submissionId);
            commandMap.put(RunnerConstants.LANGUAGE, language.getLanguage());
            commandMap.put(RunnerConstants.FILE_EXTENSION, language.getFileExtension());
            commandMap.put(RunnerConstants.COMPILE_COMMAND, language.getCompileCommand());
            commandMap.put(RunnerConstants.RUN_COMMAND, language.getRunCommand());
            commandMap.put(RunnerConstants.CLASS_NAME, attribute.getFileName());

            List<TestCase> testCases = problemRepository.findProblemByProblemCode(problemCode).getTestCases();

            AtomicBoolean compilationError = new AtomicBoolean(false);

            if (!language.getCompileCommand().isEmpty()) {
                compileCode(commandMap, compilationError);
            }

            if (compilationError.get()) {
                FileUtils.deleteDirectory(submissionDir);
                return new RunResponse(Long.parseLong(submissionId), SubmissionConstants.CE, SubmissionConstants.COMPILATION_ERROR,
                        testCases.size(), 0, 0, 0);
            }

            if (!language.getCompileCommand().isEmpty()) {
                System.out.println("Code compiled successfully");
            }

            for (TestCase testCase: testCases) {
                // set input file path and output file path
                commandMap.put(RunnerConstants.INPUT_FILE_PATH, problemTestCasesDirectory + testCase.getInputFileName());

                String outputFilePath = userOutputDirectory + submissionId + RunnerConstants.OUTPUT + testCase.getId() +
                        FileConstants.TEXT_FILE_EXT;
                File outputFile = new File(outputFilePath);
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                commandMap.put(RunnerConstants.OUTPUT_FILE_PATH, outputFilePath);

                // run test
                AtomicBoolean runTimeError = new AtomicBoolean(false);
                AtomicBoolean timeLimitExceeded = new AtomicBoolean(false);

                runTest(commandMap, runTimeError, timeLimitExceeded);

                if (runTimeError.get()) {
                    outputFile.delete();
                    FileUtils.deleteDirectory(submissionDir);
                    return new RunResponse(Long.parseLong(submissionId), SubmissionConstants.RTE, SubmissionConstants.RUN_TIME_ERROR,
                            testCases.size(), passedCount.get(), wrongAnswerCount.get(), timeLimitExceededCount.get());
                }

                if (timeLimitExceeded.get()) {
                    timeLimitExceededCount.getAndIncrement();
                }
                else {
                    String actualOutputFilePath = problemTestCasesDirectory + testCase.getOutputFileName();
                    boolean result = compareOneTest(outputFilePath, actualOutputFilePath);
                    System.out.println("Input: " + testCase.getId() + " => " + result);
                    if (result) {
                        passedCount.getAndIncrement();
                    } else {
                        wrongAnswerCount.getAndIncrement();
                    }
                }
                outputFile.delete();
            }
            FileUtils.deleteDirectory(submissionDir);
            return getRunResponse(Long.parseLong(submissionId), testCases.size(), passedCount, wrongAnswerCount, timeLimitExceededCount);
        }
        catch (Exception e) {
            System.out.println("Error while evaluating code");
            return new RunResponse(Long.parseLong(attribute.getSubmissionId()), SubmissionConstants.SYSTEM_ERROR, SubmissionConstants.SYSTEM_ERROR,
                    0, 0, 0, 0);
        }
    }

    private RunResponse getRunResponse(long submissionId, long totalTestCases, AtomicLong passedCount, AtomicLong wrongAnswerCount, AtomicLong timeLimitExceededCount) {
        RunResponse runResponse = new RunResponse();
        if (timeLimitExceededCount.get() > 0) {
            runResponse.setVerdict(SubmissionConstants.TLE);
            runResponse.setMessage(SubmissionConstants.TIME_LIMIT_EXCEEDED);
        }
        else if (passedCount.get() == totalTestCases) {
            runResponse.setVerdict(SubmissionConstants.AC);
            runResponse.setMessage(SubmissionConstants.ACCEPTED);
        }
        else {
            runResponse.setVerdict(SubmissionConstants.WA);
            runResponse.setMessage(SubmissionConstants.WRONG_ANSWER);
        }
        runResponse.setTestCasesCount(totalTestCases);
        runResponse.setPassedCount(passedCount.get());
        runResponse.setWrongAnswerCount(wrongAnswerCount.get());
        runResponse.setTimeLimitExceededCount(timeLimitExceededCount.get());
        runResponse.setId(submissionId);
        return runResponse;
    }


    private void compileCode(Map<String, String> commandMap, AtomicBoolean compilationError) {
        StringSubstitutor sub = new StringSubstitutor(commandMap);
        String compileCommand = sub.replace(commandMap.get(RunnerConstants.COMPILE_COMMAND));
        ProcessBuilder builder;
        Process p = null;
        try {
            // Process to compile the code
            builder = new ProcessBuilder();
            builder.command("bash", "-c", compileCommand);
            builder.redirectErrorStream(false);
            p = builder.start();
            checkErrorStream(p, compilationError);
            p.destroyForcibly();
        }
        catch (IOException e) {
            e.printStackTrace();
            compilationError.set(true);
            assert p != null;
            p.destroyForcibly();
        }
    }

    private void runTest(Map<String, String> commandMap, AtomicBoolean runTimeError, AtomicBoolean timeLimitExceeded){
        StringSubstitutor sub = new StringSubstitutor(commandMap);
        String runCommand = sub.replace(commandMap.get(RunnerConstants.RUN_COMMAND));
        ProcessBuilder builder;
        Process process = null;
        try {
            // Process to run and execute the code
            builder = new ProcessBuilder();
            builder.command("bash","-c", runCommand);
            builder.redirectErrorStream(false);
            process = builder.start();
            timeLimitExceeded.set(!process.waitFor(1, TimeUnit.SECONDS));
            if (!timeLimitExceeded.get()) {
                checkErrorStream(process, runTimeError);
            }
            killProcess(process);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            runTimeError.set(true);
            assert process != null;
            killProcess(process);
        }
    }

    private void killProcess(Process process) {
        if (Objects.nonNull(process.descendants())) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
        }
        process.destroyForcibly();
    }

    private long getLastModifiedDateForProblemTestCaseFolder(String problemTestCaseDirectory) {
        File hiddenTestCases = new File(problemTestCaseDirectory + FileConstants.HIDDEN_TEST);
        File sampleTestCases = new File(problemTestCaseDirectory + FileConstants.SAMPLE_TEST);
        return Math.max(hiddenTestCases.lastModified(), sampleTestCases.lastModified());
    }

}
