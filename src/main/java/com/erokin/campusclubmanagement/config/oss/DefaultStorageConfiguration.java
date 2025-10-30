package com.erokin.campusclubmanagement.config.oss;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.erokin.campusclubmanagement.service.FileStorageService;
import com.erokin.campusclubmanagement.service.impl.DisabledFileStorageService;

@Configuration
public class DefaultStorageConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    public FileStorageService fileStorageServiceFallback() {
        return new DisabledFileStorageService();
    }
}
