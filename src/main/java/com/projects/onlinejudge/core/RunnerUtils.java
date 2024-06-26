package com.projects.onlinejudge.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class RunnerUtils {

    public boolean compareOneTest(String userOutput, String actualOutput ) {
        String userText = removeSpace(userOutput);
        String actualText = removeSpace(actualOutput);
        if(userText.equals(actualText)){
            return true;
        }
        else return false;
        // only sample
    }
    public String removeSpace(String file) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(file), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    // Printing the terminal output after code execution
    public static void printResults(Process process, AtomicBoolean runTimeError) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // check error stream
        checkErrorStream(process, runTimeError);
    }

    public static void checkErrorStream(Process process, AtomicBoolean runTimeError) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line = "";
        if (!(reader.readLine() == null || Objects.equals(reader.readLine(), ""))) {
            runTimeError.set(true);
        }
    }

    // This filter will only include files starting  with input
    FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File f, String name) {
            return name.startsWith("input");
        }
    };

    public static void copyContent(File a, File b) throws Exception
    {
        FileInputStream in = new FileInputStream(a);
        FileOutputStream out = new FileOutputStream(b);
        try {
            int n;
            while ((n = in.read()) != -1) {
                out.write(n);
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        System.out.println("File Copied");
    }
}
