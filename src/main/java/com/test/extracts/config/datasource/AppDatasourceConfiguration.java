package com.test.extracts.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration("appDatasourceConfiguration")
@EnableTransactionManagement
public class AppDatasourceConfiguration {

    @Bean(name = "appDatasource", destroyMethod = "close")
    @ConfigurationProperties(prefix = "test.app.datasource")
    public DataSource appDatasource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "appTransactionManager")
    public JdbcTransactionManager appTransactionManager() {
        return new JdbcTransactionManager(appDatasource());
    }
}
