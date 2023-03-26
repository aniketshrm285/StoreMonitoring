package com.example.storemonitoring.controller;

import com.example.storemonitoring.common.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * This controller will be used to upload required CSV files to the database.
 */
@RestController
@RequestMapping("/import")
public class ImportCSVController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier(Constants.STORE_HOURS_IMPORT_JOB_NAME)
    private Job storeHoursImportJob;

    @Autowired
    @Qualifier(Constants.STORE_STATUS_IMPORT_JOB_NAME)
    private Job storeStatusImportJob;

    @Autowired
    @Qualifier(Constants.STORE_TIMEZONE_IMPORT_JOB_NAME)
    private Job storeTimezoneImportJob;

    private final String TEMP_STORAGE = "C:/Users/Aniket/Desktop/batch-files/";

    @PostMapping("/store-hours")
    public void importStoreHoursCSVToDatabase(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        importCSVToDatabase(storeHoursImportJob, multipartFile);
    }

    @PostMapping("/store-status")
    public void importStoreStatusCSVToDatabase(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        importCSVToDatabase(storeStatusImportJob, multipartFile);
    }

    @PostMapping("/store-timezone")
    public void importStoreTimezoneCSVToDatabase(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        importCSVToDatabase(storeTimezoneImportJob, multipartFile);
    }

    private void importCSVToDatabase(Job jobToLaunch, MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        File fileToImport = new File(TEMP_STORAGE+originalFileName);
        multipartFile.transferTo(fileToImport);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fullPathFileName", TEMP_STORAGE+originalFileName)
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();

        try {
            jobLauncher.run(jobToLaunch, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
