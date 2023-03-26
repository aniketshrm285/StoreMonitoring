package com.example.storemonitoring.controller;

import com.example.storemonitoring.common.Constants;
import com.example.storemonitoring.service.ImportCSVService;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * This controller will be used to upload required CSV files to the database.
 */
@RestController
@RequestMapping("/import")
public class ImportCSVController {

    @Autowired
    @Qualifier(Constants.STORE_HOURS_IMPORT_JOB_NAME)
    private Job storeHoursImportJob;

    @Autowired
    @Qualifier(Constants.STORE_STATUS_IMPORT_JOB_NAME)
    private Job storeStatusImportJob;

    @Autowired
    @Qualifier(Constants.STORE_TIMEZONE_IMPORT_JOB_NAME)
    private Job storeTimezoneImportJob;

    @Autowired
    private ImportCSVService importCSVService;

    @Value("${project.csv}")
    private String path;

    @PostMapping("/store-hours")
    public ResponseEntity<String> importStoreHoursCSVToDatabase(@RequestParam("file") MultipartFile multipartFile) {
        // validation
        if(multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Empty file");
        }
        if(!multipartFile.getContentType().equals("text/csv")) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Only CSV files are allowed");
        }
        //upload and import
        try{
            uploadAndImportCSVFile(storeHoursImportJob, multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some error happened");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("CSV file uploaded successfully");
    }

    @PostMapping("/store-status")
    public ResponseEntity<String> importStoreStatusCSVToDatabase(@RequestParam("file") MultipartFile multipartFile) {
        // validation
        if(multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Empty file");
        }
        if(!multipartFile.getContentType().equals("text/csv")) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Only CSV files are allowed");
        }
        //upload and import
        try{
            uploadAndImportCSVFile(storeStatusImportJob, multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some error happened");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("CSV file uploaded successfully");
    }

    @PostMapping("/store-timezone")
    public ResponseEntity<String> importStoreTimezoneCSVToDatabase(@RequestParam("file") MultipartFile multipartFile) {
        // validation
        if(multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Empty file");
        }
        if(!multipartFile.getContentType().equals("text/csv")) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Only CSV files are allowed");
        }
        //upload and import
        try{
            uploadAndImportCSVFile(storeTimezoneImportJob, multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some error happened");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("CSV file uploaded successfully");
    }

    private void uploadAndImportCSVFile(Job jobToLaunch, MultipartFile multipartFile) throws Exception {
        String fullFilePath = importCSVService.uploadMultipartFile(path, multipartFile);
        importCSVService.importCSVToDatabase(jobToLaunch, fullFilePath);
        importCSVService.deleteUploadedFile(fullFilePath);
    }

}
