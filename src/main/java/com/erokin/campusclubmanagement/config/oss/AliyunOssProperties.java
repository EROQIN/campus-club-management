package com.erokin.campusclubmanagement.config.oss;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.aliyun.oss.common.comm.SignVersion;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {

    /**
     * Whether OSS storage integration is enabled. Allows disabling in non-cloud environments.
     */
    private boolean enabled = false;

    /**
     * The OSS service endpoint, e.g. {@code https://oss-cn-hangzhou.aliyuncs.com}.
     */
    @NotBlank
    private String endpoint;

    /**
     * Region code, e.g. {@code cn-hangzhou}.
     */
    @NotBlank
    private String region;

    /**
     * Target bucket name.
     */
    @NotBlank
    private String bucket;

    /**
     * Optional base path that will be prefixed to every object key (e.g. {@code campus-dev}).
     */
    private String basePath = "";

    /**
     * Optional public domain bound to the bucket, e.g. CDN domain.
     * When provided, URLs are assembled with this host instead of the raw endpoint.
     */
    private String publicDomain;

    /**
     * Default signature version applied to the OSS client. Defaults to V4 according to latest OSS best practices.
     */
    private SignVersion signVersion = SignVersion.V4;

    /**
     * Default expiration for generated presigned URLs.
     */
    private Duration defaultUrlExpiry = Duration.ofHours(1);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getPublicDomain() {
        return publicDomain;
    }

    public void setPublicDomain(String publicDomain) {
        this.publicDomain = publicDomain;
    }

    public SignVersion getSignVersion() {
        return signVersion;
    }

    public void setSignVersion(SignVersion signVersion) {
        this.signVersion = signVersion;
    }

    public Duration getDefaultUrlExpiry() {
        return defaultUrlExpiry;
    }

    public void setDefaultUrlExpiry(Duration defaultUrlExpiry) {
        this.defaultUrlExpiry = defaultUrlExpiry;
    }
}
