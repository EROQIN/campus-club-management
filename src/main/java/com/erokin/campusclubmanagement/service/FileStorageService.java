package com.erokin.campusclubmanagement.service;

import java.io.InputStream;
import java.time.Duration;

import com.erokin.campusclubmanagement.dto.storage.StoredObjectInfo;

public interface FileStorageService {

    /**
     * Uploads an object to the configured cloud storage bucket.
     *
     * @param directory     logical folder (e.g. {@code qrcode/2024}), optional
     * @param objectName    preferred file name with extension
     * @param content       file stream
     * @param contentLength length of the stream in bytes, or {@code -1} if unknown
     * @param contentType   MIME type, optional
     * @return descriptor containing bucket, object key and an accessible URL
     */
    StoredObjectInfo upload(String directory, String objectName, InputStream content, long contentLength, String contentType);

    /**
     * Removes the object identified by the given key.
     *
     * @param objectKey path inside the bucket
     */
    void delete(String objectKey);

    /**
     * Builds an accessible URL from the provided object key without creating new signatures.
     * Depending on ACL configuration this may point to a public domain or the native bucket endpoint.
     */
    String buildObjectUrl(String objectKey);

    /**
     * Generates a temporary signed URL for accessing a private object.
     *
     * @param objectKey object key
     * @param ttl       validity period; when {@code null}, a default configured value will be used
     * @return presigned URL string
     */
    String generatePresignedUrl(String objectKey, Duration ttl);
}
