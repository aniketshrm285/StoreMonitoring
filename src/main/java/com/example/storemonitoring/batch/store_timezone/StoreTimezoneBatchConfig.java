package com.example.storemonitoring.batch.store_timezone;

import com.example.storemonitoring.common.Constants;
import com.example.storemonitoring.model.store_timezone.StoreTimezone;
import com.example.storemonitoring.repository.StoreTimezoneRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StoreTimezoneBatchConfig {
    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final StoreTimezoneRepository StoreTimezoneRepository;

    private final TaskExecutor taskExecutor;

    private LineMapper<StoreTimezone> lineMapper() {

        DefaultLineMapper<StoreTimezone> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(",");
        delimitedLineTokenizer.setStrict(false);

        delimitedLineTokenizer.setNames("storeId", "timezoneStr");//headers

        BeanWrapperFieldSetMapper<StoreTimezone> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(StoreTimezone.class);

        lineMapper.setLineTokenizer(delimitedLineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    @StepScope //new instance will be created for each step
    public FlatFileItemReader<StoreTimezone> storeTimezoneReader(@Value("#{jobParameters[fullPathFileName]}") String pathToFile){
        FlatFileItemReader<StoreTimezone> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(new File(pathToFile)));
        itemReader.setName("storeTimezoneReader");
        itemReader.setLinesToSkip(1);//header of CSV
        itemReader.setLineMapper(lineMapper()); // how to read CSV file and map to StoreTimezone entity
        return itemReader;
    }

    @Bean
    public StoreTimezoneProcessor storeTimezoneProcessor() {
        return new StoreTimezoneProcessor();
    }

    @Bean
    public RepositoryItemWriter<StoreTimezone> storeTimezoneWriter() {
        RepositoryItemWriter<StoreTimezone> writer = new RepositoryItemWriter<>();
        writer.setRepository(StoreTimezoneRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step storeTimezoneReadProcessWriteStep(FlatFileItemReader<StoreTimezone> reader) {
        return stepBuilderFactory.get("csv-step")
                .<StoreTimezone,StoreTimezone>chunk(Constants.STEP_BUILDER_CHUNK_SIZE)
                .reader(reader)
                .processor(storeTimezoneProcessor())
                .writer(storeTimezoneWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean(name = Constants.STORE_TIMEZONE_IMPORT_JOB_NAME)
    public Job StoreTimezoneImportJob(FlatFileItemReader<StoreTimezone> reader){
        return jobBuilderFactory.get("importStoreTimezone")
                .flow(storeTimezoneReadProcessWriteStep(reader))
                .end().build();
    }
}
