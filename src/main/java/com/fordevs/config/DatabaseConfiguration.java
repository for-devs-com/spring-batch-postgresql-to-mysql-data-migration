package com.fordevs.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing(dataSourceRef = "springBatchMetaDataDataSource")
public class DatabaseConfiguration {


    @Bean("springBatchMetaDataDataSource")
    @Primary
    public DataSource springBatchMetaDataDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/spring-batch-metadata?createDatabaseIfNotExist=TRUE");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("toor");
        return dataSourceBuilder.build();
    }

    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(springBatchMetaDataDataSource());
    }

    @Bean("entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("springBatchMetaDataDataSource") DataSource datasource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(datasource);
        em.setPackagesToScan("com.example.yourpackage");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }
//    @Override
//    @Bean
//    @Primary
////    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSource getDatasource() {
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/spring-batch-metadata?createDatabaseIfNotExist=TRUE");
//        dataSourceBuilder.username("root");
//        dataSourceBuilder.password("toor");
//        return dataSourceBuilder.build();
//    }

    @Bean("mysqluniversitydatasource")
    public DataSource mysqluniversitydatasource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/university?createDatabaseIfNotExist=TRUE");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("toor");
        return dataSourceBuilder.build();
    }

    @Bean("postgresqluniversitydatasource")
    public DataSource postgresqluniversitydatasource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/for-devs-university?createDatabaseIfNotExist=TRUE");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("toor");
        return dataSourceBuilder.build();
    }

    @Bean
    public EntityManagerFactory postgresqlEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lem = new LocalContainerEntityManagerFactoryBean();
        lem.setDataSource(postgresqluniversitydatasource());
        //In next package we have the entity classes require for postgreSql connection
        lem.setPackagesToScan("com.fordevs.entity.postgresql");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();
        return lem.getObject();
    }

    @Bean
    public EntityManagerFactory mysqlEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lem = new LocalContainerEntityManagerFactoryBean();
        lem.setDataSource(mysqluniversitydatasource());
        lem.setPackagesToScan("com.fordevs.entity.mysql");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();
        return lem.getObject();
    }

    @Bean("postgresqlTransactionManager")
    public JpaTransactionManager postgresqlTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(postgresqluniversitydatasource());
        transactionManager.setEntityManagerFactory(postgresqlEntityManagerFactory());
        return transactionManager;
    }

    @Bean("mysqlTransactionManager")
    @Primary
    public JpaTransactionManager mysqlTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(mysqluniversitydatasource());
        transactionManager.setEntityManagerFactory(mysqlEntityManagerFactory());
        return transactionManager;
    }
}