package com.fordevs.config;

import com.fordevs.entity.mysql.MySqlStudent;
import com.fordevs.entity.postgresql.PostgreSqlStudent;
import com.fordevs.processor.PostgresqlToMysqlProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.persistence.EntityManagerFactory;

@Configuration
public class PostgresqlToMysqlJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private PostgresqlToMysqlProcessor postgresqlToMysqlProcessor;
    @Autowired
    @Qualifier("postgresqlEntityManagerFactory")
    private EntityManagerFactory postgresqlEntityManagerFactory;
    @Autowired
    @Qualifier("mysqlEntityManagerFactory")
    private EntityManagerFactory mysqlEntityManagerFactory;
    @Autowired
    private JpaTransactionManager jpaTransactionManager;

    @Bean
    public Job chunkJob() {
        return jobBuilderFactory.get("Chunk Job")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .build();
    }

    private Step firstChunkStep() {
        return stepBuilderFactory.get("First Chunk Step")
                .<PostgreSqlStudent, MySqlStudent>chunk(3)
                .reader(jpaCursorItemReader())
                .processor(postgresqlToMysqlProcessor)
                .writer(jpaItemWriter())
                .faultTolerant()
                .skip(Throwable.class)
                .skipLimit(100)
                .retryLimit(3).retry(Throwable.class)
                .transactionManager(jpaTransactionManager)
                .build();
    }

    @StepScope
    @Bean
    public JpaCursorItemReader<PostgreSqlStudent> jpaCursorItemReader() {
        JpaCursorItemReader<PostgreSqlStudent> jpaCursorItemReader = new JpaCursorItemReader<>();
        jpaCursorItemReader.setEntityManagerFactory(postgresqlEntityManagerFactory);
        //Next is a query for the whole student table.
        jpaCursorItemReader.setQueryString("From PostgreSqlStudent");
        return jpaCursorItemReader;
    }

    public JpaItemWriter<MySqlStudent> jpaItemWriter() {
        JpaItemWriter<MySqlStudent> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(mysqlEntityManagerFactory);
        return jpaItemWriter;
    }
}
