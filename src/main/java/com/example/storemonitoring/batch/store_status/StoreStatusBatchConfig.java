package com.example.storemonitoring.batch.store_status;

import com.example.storemonitoring.common.Constants;
import com.example.storemonitoring.model.store_status.StoreStatus;
import com.example.storemonitoring.repository.StoreStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;

import java.io.File;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class StoreStatusBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final StoreStatusRepository storeStatusRepository;

    private final TaskExecutor taskExecutor;

    private LineMapper<StoreStatus> lineMapper() {

        DefaultLineMapper<StoreStatus> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(",");
        delimitedLineTokenizer.setStrict(false);

        delimitedLineTokenizer.setNames("storeId","status","timestampUTC");//headers

        BeanWrapperFieldSetMapper<StoreStatus> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(StoreStatus.class);

        lineMapper.setLineTokenizer(delimitedLineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    @StepScope //new instance will be created for each step
    public FlatFileItemReader<StoreStatus> reader(@Value("#{jobParameters[fullPathFileName]}") String pathToFile){
        FlatFileItemReader<StoreStatus> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(new File(pathToFile)));
        itemReader.setName("storeStatusReader");
        itemReader.setLinesToSkip(1);//header of CSV
        itemReader.setLineMapper(lineMapper()); // how to read CSV file and map to StoreStatus entity
        return itemReader;
    }

    @Bean
    public StoreStatusProcessor processor() {
        return new StoreStatusProcessor();
    }

    @Bean
    public RepositoryItemWriter<StoreStatus> writer() {
        RepositoryItemWriter<StoreStatus> writer = new RepositoryItemWriter<>();
        writer.setRepository(storeStatusRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step readProcessWriteStep(FlatFileItemReader<StoreStatus> reader) {
        return stepBuilderFactory.get("csv-step")
                .<StoreStatus,StoreStatus>chunk(Constants.STEP_BUILDER_CHUNK_SIZE)
                .reader(reader)
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean(name = Constants.STORE_STATUS_IMPORT_JOB_NAME)
    public Job storeStatusImportJob(FlatFileItemReader<StoreStatus> reader){
        return jobBuilderFactory.get("importStoreStatus")
                .flow(readProcessWriteStep(reader))
                .end().build();
    }

    
}
