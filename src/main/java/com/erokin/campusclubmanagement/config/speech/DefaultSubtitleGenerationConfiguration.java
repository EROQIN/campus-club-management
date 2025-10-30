package com.erokin.campusclubmanagement.config.speech;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.service.SubtitleGenerationService;

@Configuration
public class DefaultSubtitleGenerationConfiguration {

    @Bean
    @ConditionalOnMissingBean(SubtitleGenerationService.class)
    public SubtitleGenerationService disabledSubtitleGenerationService() {
        return mediaUrl -> {
            throw new BusinessException("AI 字幕功能未启用，请联系管理员配置阿里云语音识别服务。");
        };
    }
}
