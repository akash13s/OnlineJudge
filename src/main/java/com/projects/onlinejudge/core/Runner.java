package com.projects.onlinejudge.core;

import com.projects.onlinejudge.config.LanguageConfig;
import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.constants.RunnerConstants;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Runner extends RunnerUtils {

    //
    @Value("${submissions.directory}")
    private String submissionsDirectory;

    // looks good
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
        AtomicLong passedCount = new AtomicLong();
        AtomicLong failedCount = new AtomicLong();
        try {

            String lang = attribute.getLanguage();
            String userName = attribute.getUserName();
            String submissionId = attribute.getSubmissionId();
            String problemCode = attribute.getProblemCode();

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

            Map<String, String> commandMap = new HashMap<>();
            commandMap.put(RunnerConstants.SOURCE_FILE_DIRECTORY, submissionDir.getAbsolutePath());
            commandMap.put(RunnerConstants.SUBMISSION_ID, submissionId);
            commandMap.put(RunnerConstants.LANGUAGE, language.getLanguage());
            commandMap.put(RunnerConstants.FILE_EXTENSION, language.getFileExtension());
            commandMap.put(RunnerConstants.COMPILE_COMMAND, language.getCompileCommand());
            commandMap.put(RunnerConstants.RUN_COMMAND, language.getRunCommand());
            commandMap.put(RunnerConstants.CLASS_NAME, attribute.getFileName());

            boolean compilationSuccess = true;

            if (!language.getCompileCommand().isEmpty()) {
                compilationSuccess = compileCode(commandMap);
            }

            if (!compilationSuccess) {
                return new RunResponse("Compilation Error", 0, 0);
            }

            List<TestCase> testCases = problemRepository.findProblemByProblemCode(problemCode).getTestCases();
            AtomicBoolean accepted = new AtomicBoolean(true);

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
                boolean runStatus = runTest(commandMap);
                if (!runStatus) {
                    return new RunResponse("Runtime error", passedCount.get(), failedCount.get());
                }

                String actualOutputFilePath = problemTestCasesDirectory + testCase.getOutputFileName();
                String result = compareOneTest(outputFilePath, actualOutputFilePath);
                System.out.println("Input: " + testCase.getId() + " => " + result);
                if (result.equals("Passed")) {
                    passedCount.getAndIncrement();
                } else {
                    accepted.set(false);
                    failedCount.getAndIncrement();
                }
                outputFile.delete();
            }
            FileUtils.deleteDirectory(submissionDir);
            return new RunResponse(accepted.get()? "Passed": "Failed", passedCount.get(), failedCount.get());
        }
        catch (Exception e) {
            System.out.println("Error while evaluating code");
            return new RunResponse("Error while evaluating", 0, 0);
        }
    }

    public boolean compileCode(Map<String, String> commandMap) {
        StringSubstitutor sub = new StringSubstitutor(commandMap);
        String compileCommand = sub.replace(commandMap.get(RunnerConstants.COMPILE_COMMAND));
        try {
            // Process to compile the code
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("bash", "-c", compileCommand);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            printResults(p);
            System.out.println("Code Compiled Successfully");
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean runTest(Map<String, String> commandMap){
        StringSubstitutor sub = new StringSubstitutor(commandMap);
        String runCommand = sub.replace(commandMap.get(RunnerConstants.RUN_COMMAND));
        try {
            // Process to run and execute the code
            ProcessBuilder builder2 = new ProcessBuilder();
            builder2.command("bash","-c", runCommand);
            builder2.redirectErrorStream(true);
            Process p2 = builder2.start();
            printResults(p2);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
