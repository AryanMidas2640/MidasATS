package com.midas.consulting.service.crawlers;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Service
public class VectorEmailNotificationCrawlerService {

    @Async
    public void startCrawler(String baseDir, String link) {
        try {
            ProcessBuilder processBuilder = null;
            if ("Windows".equals("Linux")){
                String statusVectorJobDir = baseDir + "\\statusvectorjob";
                String command = String.format("cd %s && venv\\Scripts\\activate && cd %s && scrapy crawl statusvectorjob -a url=" + link, baseDir, statusVectorJobDir);

                processBuilder=    new ProcessBuilder("cmd.exe", "/c", command);
                processBuilder.redirectErrorStream(true);
            }
            else {
                String statusVectorJobDir = baseDir + "/statusvectorjob";
                String activateScript = statusVectorJobDir + "/venv/bin/activate"; // Adjust according to your virtual environment setup
                String command = String.format("cd %s && source %s && cd %s && scrapy crawl statusvectorjob -a url=%s", baseDir, activateScript, (statusVectorJobDir+"/statusvectorjob"), link);
                System.out.println("baseDir "+ baseDir);
                System.out.println("activateScript "+ activateScript);
                System.out.println("Command "+ command);
                processBuilder = new ProcessBuilder("bash", "-c", command);
                Thread.sleep(5000);
                processBuilder.redirectErrorStream(true);
            }

            // Print environment variables
            Map<String, String> env = processBuilder.environment();
            for (String envName : env.keySet()) {
                System.out.println(envName + "=" + env.get(envName));
            }

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("OUTPUT: " + line);
            }

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
