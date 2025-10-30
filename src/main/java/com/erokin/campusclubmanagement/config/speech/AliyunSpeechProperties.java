package com.erokin.campusclubmanagement.config.speech;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aliyun.speech")
public class AliyunSpeechProperties {

    private boolean enabled = false;

    private String regionId = "cn-shanghai";

    private String endpointName = "cn-shanghai";

    private String product = "nls-filetrans";

    private String domain = "filetrans.cn-shanghai.aliyuncs.com";

    private String apiVersion = "2018-08-17";

    private String version = "4.0";

    private String appKey;

    private String accessKeyId;

    private String accessKeySecret;

    private boolean enableWords = true;

    private Duration pollInterval = Duration.ofSeconds(3);

    private int maxPollAttempts = 40;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public boolean isEnableWords() {
        return enableWords;
    }

    public void setEnableWords(boolean enableWords) {
        this.enableWords = enableWords;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public int getMaxPollAttempts() {
        return maxPollAttempts;
    }

    public void setMaxPollAttempts(int maxPollAttempts) {
        this.maxPollAttempts = maxPollAttempts;
    }
}
