package com.example.batchprocessing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlatFileItemReader<Person> csvReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personCsvItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names("firstName", "lastName")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Person> dbReader(DataSource dataSource) {
        MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
        provider.setSelectClause("select person_id,first_name,last_name");
        provider.setFromClause("people");
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("person_id", Order.ASCENDING);
        provider.setSortKeys(sortKeys);
        return new JdbcPagingItemReaderBuilder<Person>()
                .name("personDbItemReader")
                .dataSource(dataSource)
                .queryProvider(provider)
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .pageSize(1) //分页数
                .build();
    }

    @Bean
    public PersonToUpperCaseItemProcessor toUpperCaseItemProcessor() {
        return new PersonToUpperCaseItemProcessor();
    }

    @Bean
    public PersonToLowerCaseItemProcessor toLowerCaseItemProcessor() {
        return new PersonToLowerCaseItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> dbWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Person> csvWriter() {
        return new FlatFileItemWriterBuilder<Person>()
                .name("personCsvWriter")
                .resource(new FileSystemResource("db-data.csv"))
                .delimited()
                .names("firstName", "lastName")
                .build();
    }

    @Bean
    public Job importPeopleJob(JobCompletionNotificationListener listener, Step importPeopleStep) {
        return jobBuilderFactory.get("importPeopleJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(importPeopleStep)
                .end()
                .build();
    }

    @Bean
    public Job exportPeopleJob(JobCompletionNotificationListener listener, Step exportPeopleStep) {
        return jobBuilderFactory.get("exportPeopleJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(exportPeopleStep)
                .end()
                .build();
    }

    @Bean
    public Step importPeopleStep(FlatFileItemReader<Person> reader,
                                 PersonToUpperCaseItemProcessor processor,
                                 JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("importPeopleStep")
                .<Person, Person>chunk(10) //分片大小
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step exportPeopleStep(JdbcPagingItemReader<Person> reader,
                                 PersonToLowerCaseItemProcessor processor,
                                 FlatFileItemWriter<Person> writer) {
        return stepBuilderFactory.get("exportPeopleStep")
                .<Person, Person>chunk(1) //分片大小
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(new SimpleAsyncTaskExecutor()) //并行处理
                .build();
    }
}
