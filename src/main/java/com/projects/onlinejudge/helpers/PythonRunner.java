package com.projects.onlinejudge.helpers;

import com.projects.onlinejudge.dto.RunRequest;
import com.projects.onlinejudge.dto.RunResponse;
import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PythonRunner extends IRunner{
    @Override
    public RunResponse runTests(RunRequest attribute)  {
        Integer passedCount =0 , failedCount =0;
        try {

            // TODO : service2.call(attribute) For future
            String lang= attribute.getLang();
            String userId= attribute.getUserId();
            String submissionId= attribute.getSubmissionId();
            String name_of_the_problem = attribute.getQname();

            // Code is compiled and I got the executable file
            String filePath = "/Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/tempFiles/";
            File sourceFile = new File(filePath.trim()+"sourceFile.txt");
            File cppFile =  new File(filePath.trim()+name_of_the_problem+".py");
            copyContent(sourceFile,cppFile);

            // Count the total input files
            File questionDirectory = new File("/Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/Questions/"+ name_of_the_problem +"/tests");

            String[] pathnames = questionDirectory.list(filter);
            int size = pathnames.length;
            ArrayList<Pair<Integer,String>> testResults = new ArrayList<>();
            for ( int input = 1; input <= size; input++){
                /*
                    Input File: /Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/Questions/Addition/tests/input-1.txt
                    User OutputFile: /Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/Questions/Addition/tests/user-output-1.txt

                 */
                String inputFile="/Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/Questions/"+name_of_the_problem+"/tests/input-" + input+".txt";
                String useroutputFile="/Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/Questions/"+name_of_the_problem+"/tests/user-output-" + input+".txt";
                String actualoutputFile="/Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/Questions/"+name_of_the_problem+"/tests/output-" + input+".txt";
//
                run_one_test(name_of_the_problem,inputFile,useroutputFile);
                String result = compare_one_test(useroutputFile,actualoutputFile);
                System.out.println("Input: "+input +" => " + result);
                Pair<Integer,String> testResult = new Pair<>(input,result);
                testResults.add(testResult);
                if(result.equals("Passed")) {
                    passedCount++;
                } else {
                    failedCount++;
                }

            }
            RunResponse runResponse = new RunResponse(passedCount, failedCount,testResults);
            return runResponse;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void run_one_test(String nameOfProblem, String  input_file_name, String output_file_name){
        String filePath = "/Users/amankumarkeshu/Documents/Projects/codefoxes/OnlineJudge/tempFiles/";

        String runAndOutput = "cd "+ filePath+" && python3 "+nameOfProblem + ".py < " + input_file_name + " > " + output_file_name;
        try {
            // Process to run and execute the code
            ProcessBuilder builder2 = new ProcessBuilder();
            builder2.command("bash","-c", runAndOutput);
            builder2.redirectErrorStream(true);
            Process p2 = builder2.start();
            printResults(p2);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
