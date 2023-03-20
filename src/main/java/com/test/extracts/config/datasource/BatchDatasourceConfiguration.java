package com.test.extracts.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class BatchDatasourceConfiguration {

    @Bean(name = "batchDataSource", destroyMethod = "close")
    @ConfigurationProperties(prefix = "test.batch.datasource")
    public HikariDataSource taskDatasource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }


    @Bean(name = "batchTransactionManager")

    public PlatformTransactionManager taskTransactionManager() {
        return new JdbcTransactionManager(taskDatasource());
    }


    @Bean
    @Qualifier("taskTemplate")
    public JdbcTemplate taskTemplate() {
        return new JdbcTemplate(taskDatasource());
    }


}
