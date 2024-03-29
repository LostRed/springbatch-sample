package com.example.batchprocessing;

import com.example.mybatis.RecordDeleteWriter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
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
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public MyBatisPagingItemReader<Person> personPagingItemReader(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisPagingItemReaderBuilder<Person>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.mybatis.PersonMapper.selectAll")
                .pageSize(1)
                .build();
    }

    @Bean
    public MyBatisCursorItemReader<Person> personCursorItemReader(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisCursorItemReaderBuilder<Person>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.mybatis.PersonMapper.selectCursor")
                .build();
    }

    @Bean
    public MyBatisBatchItemWriter<Record> recordDeleteItemWriter(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<Record>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.example.mybatis.RecordMapper.delete")
                .assertUpdates(false)
                .build();
    }

    @Bean
    public MyBatisBatchItemWriter<List<Record>> recordBatchInsertItemWriter(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisBatchItemWriterBuilder<List<Record>>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.example.mybatis.RecordMapper.insertBatch")
                .assertUpdates(false)
                .build();
    }

    @Bean
    public CompositeItemWriter<List<Record>> compositeItemWriter(RecordDeleteWriter recordDeleteWriter,
                                                                 MyBatisBatchItemWriter<List<Record>> recordBatchInsertItemWriter) {
        CompositeItemWriter<List<Record>> compositeItemWriter = new CompositeItemWriter<>();
        List<ItemWriter<? super List<Record>>> writers = new ArrayList<>(2);
        writers.add(recordDeleteWriter);
        writers.add(recordBatchInsertItemWriter);
        compositeItemWriter.setDelegates(writers);
        return compositeItemWriter;
    }

    @Bean
    public Job recordJob(JobCompletionNotificationListener listener, Step recordStep) {
        return jobBuilderFactory.get("recordJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(recordStep)
                .end()
                .build();
    }

    @Bean
    public Step recordStep(MyBatisCursorItemReader<Person> reader,
                           PersonItemProcessor processor,
                           CompositeItemWriter<List<Record>> writer) {
        return stepBuilderFactory.get("recordStep")
                .<Person, List<Record>>chunk(1) //分片大小
                .reader(reader)
                .processor(processor)
                .writer(writer)
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
