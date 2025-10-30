package com.erokin.campusclubmanagement.dto.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResponse {
    private final String bucket;
    private final String objectKey;
    private final String url;
    private final String fileName;
    private final long size;
    private final String contentType;
}
