package com.erokin.campusclubmanagement.config.speech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;

import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.service.SubtitleGenerationService;
import com.erokin.campusclubmanagement.service.impl.AliyunSubtitleGenerationService;

@Configuration
@ConditionalOnClass(IAcsClient.class)
@ConditionalOnProperty(prefix = "aliyun.speech", name = "enabled", havingValue = "true")
public class AliyunSpeechConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AliyunSpeechConfiguration.class);

    @Bean
    public IAcsClient aliyunSpeechClient(AliyunSpeechProperties properties) {
        String accessKeyId = resolveAccessKey(properties.getAccessKeyId(), "ALIYUN_AK_ID");
        String accessKeySecret = resolveAccessKey(properties.getAccessKeySecret(), "ALIYUN_AK_SECRET");
        if (!StringUtils.hasText(properties.getAppKey())) {
            throw new BusinessException("未配置阿里云语音识别 AppKey，请在配置文件或环境变量中提供 NLS_APP_KEY。");
        }
        try {
            DefaultProfile.addEndpoint(
                    properties.getEndpointName(), properties.getRegionId(), properties.getProduct(), properties.getDomain());
            DefaultProfile profile = DefaultProfile.getProfile(properties.getRegionId(), accessKeyId, accessKeySecret);
            log.info("Aliyun speech client initialized for region '{}', endpoint '{}'", properties.getRegionId(), properties.getDomain());
            return new DefaultAcsClient(profile);
        } catch (ClientException e) {
            log.error("Failed to initialize Aliyun speech client: {}", e.getMessage(), e);
            throw new BusinessException("初始化阿里云语音识别客户端失败，请检查凭证与网络配置。");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SubtitleGenerationService subtitleGenerationService(IAcsClient client, AliyunSpeechProperties properties) {
        return new AliyunSubtitleGenerationService(client, properties);
    }

    private String resolveAccessKey(String configuredValue, String envName) {
        if (StringUtils.hasText(configuredValue)) {
            return configuredValue.trim();
        }
        String envValue = System.getenv(envName);
        if (StringUtils.hasText(envValue)) {
            return envValue.trim();
        }
        throw new BusinessException(String.format("未配置阿里云访问凭证，请在配置文件中提供 %s 或设置环境变量 %s。", configuredValueName(envName), envName));
    }

    private String configuredValueName(String envName) {
        if ("ALIYUN_AK_ID".equals(envName)) {
            return "aliyun.speech.access-key-id";
        }
        if ("ALIYUN_AK_SECRET".equals(envName)) {
            return "aliyun.speech.access-key-secret";
        }
        return envName.toLowerCase();
    }
}
