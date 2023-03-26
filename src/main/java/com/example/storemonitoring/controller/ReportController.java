package com.example.storemonitoring.controller;

import com.example.storemonitoring.model.ReportModel;
import com.example.storemonitoring.service.ReportService;
import com.example.storemonitoring.util.CustomMappingStrategyReportCSV;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This controller will be used to handle API requests to trigger and get report.
 */
@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    /**This map will be used to store report completables for different report Ids*/
    private final HashMap<String,CompletableFuture<List<ReportModel>>> reportCompletableMap = new HashMap<>();
    private String lastReportHashCode;

    /**
     * This method will be used to trigger report generation.
     * Will fire off a completable and return it's hashcode as report id.
     * @return
     */
    @PostMapping("/trigger_report")
    public ResponseEntity<String> triggerReport(){
        if(lastReportHashCode == null || reportCompletableMap.get(lastReportHashCode).isDone()) {
            CompletableFuture<List<ReportModel>> reportCompletable = CompletableFuture.supplyAsync(
                    () -> {
                        reportService.clearAllInMemoryData();
                        try {
                            return reportService.generateReport();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            String hc = String.valueOf(reportCompletable.hashCode());
            lastReportHashCode = hc;
            reportCompletableMap.put(hc, reportCompletable);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(hc);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Report already triggered for reportId: "+ lastReportHashCode +
                    ". Please wait some time to trigger another report.");
        }
    }

    /**
     * This method will be used to download report, if generated for a report id.
     * @param reportId
     * @param response
     * @throws Exception
     */
    @GetMapping("/get_report/{reportId}")
    public void getReport(@PathVariable String reportId, HttpServletResponse response) throws Exception {
        if(reportId == null || reportId.isBlank() || reportCompletableMap.get(reportId) == null){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Invalid report id.");
            return;
        }
        CompletableFuture<List<ReportModel>> reportCompletable = reportCompletableMap.get(reportId);
        if(!reportCompletable.isDone()){
            response.getWriter().write("Running");
            return;
        }
        String filename = "report_"+reportId+".csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");

        CustomMappingStrategyReportCSV<ReportModel> mappingStrategy = new CustomMappingStrategyReportCSV<>();
        mappingStrategy.setType(ReportModel.class);
        // create a csv writer using openCSV
        StatefulBeanToCsv<ReportModel> writer =
                new StatefulBeanToCsvBuilder<ReportModel>
                        (response.getWriter())
                        .withMappingStrategy(mappingStrategy)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withOrderedResults(true).build();

        writer.write(reportCompletable.get());
    }
}
