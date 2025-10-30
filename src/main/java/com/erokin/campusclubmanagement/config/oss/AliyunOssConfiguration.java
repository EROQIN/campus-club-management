package com.erokin.campusclubmanagement.config.oss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;

import com.erokin.campusclubmanagement.exception.StorageException;
import com.erokin.campusclubmanagement.service.FileStorageService;
import com.erokin.campusclubmanagement.service.impl.AliyunOssFileStorageService;

@Configuration
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class AliyunOssConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssConfiguration.class);

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(AliyunOssProperties properties) {
        try {
            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(properties.getSignVersion());
            log.info("Initializing Aliyun OSS client for bucket '{}' in region '{}'", properties.getBucket(), properties.getRegion());
            return OSSClientBuilder.create()
                .endpoint(properties.getEndpoint())
                .region(properties.getRegion())
                .credentialsProvider(CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider())
                .clientConfiguration(clientBuilderConfiguration)
                .build();
        } catch (Exception ex) {
            throw new StorageException("无法初始化阿里云 OSS 客户端，请检查凭证与配置。", ex);
        }
    }

    @Bean
    public FileStorageService fileStorageService(OSS ossClient, AliyunOssProperties properties) {
        return new AliyunOssFileStorageService(ossClient, properties);
    }
}
