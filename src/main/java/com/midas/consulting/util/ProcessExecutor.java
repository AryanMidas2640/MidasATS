package com.midas.consulting.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessExecutor {

    public static void main(String[] args) {
        try {
            String baseDir = "C:\\Codebase\\server-vector-scrapper";
            String statusVectorJobDir = baseDir + "\\statusvectorjob";

            // Command to change directory, activate venv, and start the crawler
            String command = String.format("cd %s && venv\\Scripts\\activate && cd %s && scrapy crawl statusvectorjob -a url=https://vms.vectorvms.com/7000/7020.aspx?reqID=739647", baseDir, statusVectorJobDir);

            // Create the process builder and set the command
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr

            // Start the process
            Process process = processBuilder.start();

            // Capture the output stream (stdout and stderr)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("OUTPUT: " + line);
            }

            // Wait for the process to complete
            int exitStatus = process.waitFor();
            System.out.println("Process exited with status: " + exitStatus);

            if (exitStatus == 0) {
                System.out.println("Crawler started successfully.");
            } else {
                System.out.println("Failed to start crawler.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
