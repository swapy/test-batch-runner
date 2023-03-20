package com.test.extracts.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBatchConfiguration {

    @Bean(name = "appPurchaseConfig")
    @ConfigurationProperties(prefix = "test.app.config.purchase")
    public BatchConfiguration appPurchaseConfig() {
        return new BatchConfiguration();
    }
}
