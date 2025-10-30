package com.erokin.campusclubmanagement.service.impl;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;

import com.erokin.campusclubmanagement.config.oss.AliyunOssProperties;
import com.erokin.campusclubmanagement.dto.storage.StoredObjectInfo;
import com.erokin.campusclubmanagement.exception.StorageException;
import com.erokin.campusclubmanagement.service.FileStorageService;

public class AliyunOssFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssFileStorageService.class);

    private final OSS ossClient;
    private final AliyunOssProperties properties;

    public AliyunOssFileStorageService(OSS ossClient, AliyunOssProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    @Override
    public StoredObjectInfo upload(String directory, String objectName, InputStream content, long contentLength, String contentType) {
        Assert.notNull(content, "content stream must not be null");

        String sanitizedFilename = sanitizeFilename(objectName);
        String objectKey = joinSegments(properties.getBasePath(), directory, sanitizedFilename);

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            if (contentLength >= 0) {
                metadata.setContentLength(contentLength);
            }
            if (StringUtils.hasText(contentType)) {
                metadata.setContentType(contentType);
            }
            metadata.setCacheControl("max-age=31536000, public");
            PutObjectRequest request = new PutObjectRequest(properties.getBucket(), objectKey, content, metadata);
            ossClient.putObject(request);
            String url = buildObjectUrl(objectKey);
            log.debug("Uploaded object to OSS bucket='{}', key='{}'", properties.getBucket(), objectKey);
            return new StoredObjectInfo(properties.getBucket(), objectKey, url);
        } catch (OSSException | ClientException ex) {
            throw wrap("上传对象到 OSS 失败", ex);
        }
    }

    @Override
    public void delete(String objectKey) {
        Assert.hasText(objectKey, "objectKey must not be blank");
        try {
            ossClient.deleteObject(properties.getBucket(), objectKey);
            log.debug("Deleted object from OSS bucket='{}', key='{}'", properties.getBucket(), objectKey);
        } catch (OSSException | ClientException ex) {
            throw wrap("删除 OSS 对象失败", ex);
        }
    }

    @Override
    public String buildObjectUrl(String objectKey) {
        Assert.hasText(objectKey, "objectKey must not be blank");
        String normalizedKey = normalizeKey(objectKey);

        if (StringUtils.hasText(properties.getPublicDomain())) {
            String base = properties.getPublicDomain().trim();
            if (!base.startsWith("http://") && !base.startsWith("https://")) {
                base = "https://" + base;
            }
            return stripTrailingSlash(base) + "/" + normalizedKey;
        }

        URI endpoint = URI.create(properties.getEndpoint());
        String scheme = endpoint.getScheme() != null ? endpoint.getScheme() : "https";
        String host = endpoint.getHost();
        if (!StringUtils.hasText(host)) {
            host = stripProtocol(properties.getEndpoint());
        }
        return scheme + "://" + properties.getBucket() + "." + host + "/" + normalizedKey;
    }

    @Override
    public String generatePresignedUrl(String objectKey, Duration ttl) {
        Assert.hasText(objectKey, "objectKey must not be blank");
        Duration effectiveTtl = ttl != null ? ttl : properties.getDefaultUrlExpiry();
        Instant expireAt = Instant.now().plus(effectiveTtl);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), normalizeKey(objectKey));
        request.setExpiration(Date.from(expireAt));
        return ossClient.generatePresignedUrl(request).toExternalForm();
    }

    private String sanitizeFilename(String originalName) {
        String baseName = StringUtils.hasText(originalName) ? originalName : UUID.randomUUID().toString();
        baseName = baseName.replace("\\", "/");
        if (baseName.contains("/")) {
            baseName = Arrays.stream(baseName.split("/"))
                .filter(StringUtils::hasText)
                .reduce((first, second) -> second)
                .orElse(baseName);
        }
        if (!StringUtils.hasText(baseName)) {
            baseName = UUID.randomUUID().toString();
        }
        // OSS 不允许出现换行等特殊字符，统一转为安全字符
        baseName = baseName.replaceAll("[\\s]+", "_");
        baseName = baseName.replaceAll("[^\\w\\-.]", "");
        if (!StringUtils.hasText(baseName)) {
            baseName = UUID.randomUUID().toString();
        }
        return baseName;
    }

    private String joinSegments(String... segments) {
        String joined = Arrays.stream(segments)
            .filter(StringUtils::hasText)
            .map(segment -> segment.replace("\\", "/"))
            .map(this::trimSlashes)
            .collect(Collectors.joining("/"));
        return joined;
    }

    private String trimSlashes(String value) {
        String result = value;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String stripTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String stripProtocol(String value) {
        String sanitized = value;
        if (sanitized.startsWith("https://")) {
            sanitized = sanitized.substring(8);
        } else if (sanitized.startsWith("http://")) {
            sanitized = sanitized.substring(7);
        }
        return stripTrailingSlash(sanitized);
    }

    private String normalizeKey(String objectKey) {
        String key = objectKey.replace("\\", "/");
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        return key;
    }

    private StorageException wrap(String message, Exception exception) {
        if (exception instanceof OSSException ossException) {
            log.error("Aliyun OSS error - code: {}, requestId: {}, hostId: {}", ossException.getErrorCode(), ossException.getRequestId(), ossException.getHostId());
        } else if (exception instanceof ClientException clientException) {
            log.error("Aliyun OSS client error: {}", clientException.getMessage());
        }
        return new StorageException(message + "。错误信息：" + exception.getMessage(), exception);
    }
}
