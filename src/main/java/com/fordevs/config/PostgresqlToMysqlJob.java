package com.fordevs.config;

import com.fordevs.entity.mysql.MySqlStudent;
import com.fordevs.entity.postgresql.PostgreSqlStudent;
import com.fordevs.processor.PostgresqlToMysqlProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PostgresqlToMysqlJob {

    @Autowired
    private PostgresqlToMysqlProcessor postgresqlToMysqlProcessor;

    @Autowired
    @Qualifier("postgresqlEntityManagerFactory")
    private EntityManagerFactory postgresqlEntityManagerFactory;

    @Autowired
    @Qualifier("mysqlEntityManagerFactory")
    private EntityManagerFactory mysqlEntityManagerFactory;

    @Autowired
    @Qualifier("postgresqlTransactionManager")
    private PlatformTransactionManager postgresqlTransactionManager;

    @Autowired
    @Qualifier("mysqlTransactionManager")
    private PlatformTransactionManager mysqlTransactionManager;

    @Bean
    public Job chunkJob(JobRepository jobRepository) {
        return new JobBuilder("Chunk Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep(jobRepository))
                .build();
    }

    private Step firstChunkStep(JobRepository jobRepository) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<PostgreSqlStudent, MySqlStudent>chunk(10, postgresqlTransactionManager)
                .reader(jpaCursorItemReader())
                .processor(postgresqlToMysqlProcessor)
                .writer(jpaItemWriter())
                .transactionManager(mysqlTransactionManager)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<PostgreSqlStudent> jpaCursorItemReader() {
        JpaCursorItemReader<PostgreSqlStudent> reader = new JpaCursorItemReader<>();
        reader.setEntityManagerFactory(postgresqlEntityManagerFactory);
        reader.setQueryString("SELECT s FROM PostgreSqlStudent s");
        return reader;
    }

    @Bean
    @StepScope
    public JpaItemWriter<MySqlStudent> jpaItemWriter() {
        JpaItemWriter<MySqlStudent> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(mysqlEntityManagerFactory);
        return writer;
    }
}
