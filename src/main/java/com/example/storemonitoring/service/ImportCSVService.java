package com.example.storemonitoring.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ImportCSVService {

    @Autowired
    private JobLauncher jobLauncher;

    public String uploadMultipartFile(String path, MultipartFile file) throws IOException {
        //File-name
        String name = file.getOriginalFilename();
        //Full path
        String filePath = path+name;
        //create folder if not created
        File newFile = new File(path);
        if(!newFile.exists()) {
            newFile.mkdir();
        }
        //file copy
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return filePath;
    }

    public void importCSVToDatabase(Job jobToLaunch, String filePath) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fullPathFileName", filePath)
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();

        try {
            jobLauncher.run(jobToLaunch, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void deleteUploadedFile(String fullFilePath) throws Exception {
        boolean value = Files.deleteIfExists(Paths.get(fullFilePath));
        if(!value) {
            throw new IOException("Delete wasn't successful");
        }

    }
}
