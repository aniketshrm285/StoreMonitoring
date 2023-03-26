package com.example.storemonitoring.batch.store_hours;

import com.example.storemonitoring.common.Constants;
import com.example.storemonitoring.model.store_hours.StoreHours;
import com.example.storemonitoring.repository.StoreHoursRepository;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.io.File;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class StoreHoursBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final StoreHoursRepository storeHoursRepository;

    private LineMapper<StoreHours> lineMapper() {

        DefaultLineMapper<StoreHours> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(",");
        delimitedLineTokenizer.setStrict(false);

        delimitedLineTokenizer.setNames("storeId","day","startTimeLocal","endTimeLocal");//headers

        BeanWrapperFieldSetMapper<StoreHours> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(StoreHours.class);

        lineMapper.setLineTokenizer(delimitedLineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    @StepScope //new instance will be created for each step
    public FlatFileItemReader<StoreHours> storeHoursReader(@Value("#{jobParameters[fullPathFileName]}") String pathToFile){
        FlatFileItemReader<StoreHours> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(new File(pathToFile)));
        itemReader.setName("storeHoursReader");
        itemReader.setLinesToSkip(1);//header of CSV
        itemReader.setLineMapper(lineMapper()); // how to read CSV file and map to StoreHours entity
        return itemReader;
    }

    @Bean
    public StoreHoursProcessor storeHoursProcessor() {
        return new StoreHoursProcessor();
    }

    @Bean
    public RepositoryItemWriter<StoreHours> storeHoursWriter() {
        RepositoryItemWriter<StoreHours> writer = new RepositoryItemWriter<>();
        writer.setRepository(storeHoursRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step storeHoursReadProcessWriteStep(FlatFileItemReader<StoreHours> reader) {
        return stepBuilderFactory.get("csv-step")
                .<StoreHours,StoreHours>chunk(Constants.STEP_BUILDER_CHUNK_SIZE)
                .reader(reader)
                .processor(storeHoursProcessor())
                .writer(storeHoursWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean(name = Constants.STORE_HOURS_IMPORT_JOB_NAME)
    public Job storeHoursImportJob(FlatFileItemReader<StoreHours> reader){
        return jobBuilderFactory.get("importStoreHours")
                .flow(storeHoursReadProcessWriteStep(reader))
                .end().build();
    }

    /**
     * By default, string batch write data sequentially(will take more time)
     * To better use resources, we are making use of multiple threads using TaskExecutor
     * @return
     */
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(Constants.NUMBER_OF_THREAD_FOR_CONCURRENT_EXECUTION);
        return asyncTaskExecutor;
    }
}
