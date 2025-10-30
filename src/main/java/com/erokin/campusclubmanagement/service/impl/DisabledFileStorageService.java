package com.erokin.campusclubmanagement.service.impl;

import java.io.InputStream;
import java.time.Duration;

import com.erokin.campusclubmanagement.dto.storage.StoredObjectInfo;
import com.erokin.campusclubmanagement.exception.StorageException;
import com.erokin.campusclubmanagement.service.FileStorageService;

public class DisabledFileStorageService implements FileStorageService {

    private StorageException disabledException() {
        return new StorageException("未启用云存储，请联系管理员配置阿里云 OSS 参数后再试。");
    }

    @Override
    public StoredObjectInfo upload(
            String directory, String objectName, InputStream content, long contentLength, String contentType) {
        throw disabledException();
    }

    @Override
    public void delete(String objectKey) {
        throw disabledException();
    }

    @Override
    public String buildObjectUrl(String objectKey) {
        throw disabledException();
    }

    @Override
    public String generatePresignedUrl(String objectKey, Duration ttl) {
        throw disabledException();
    }
}
