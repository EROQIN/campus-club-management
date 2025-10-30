package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.storage.StoredObjectInfo;
import com.erokin.campusclubmanagement.dto.storage.UploadResponse;
import com.erokin.campusclubmanagement.exception.StorageException;
import com.erokin.campusclubmanagement.service.FileStorageService;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class StorageController {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final DateTimeFormatter PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileStorageService fileStorageService;

    public StorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImage(
            @RequestPart("file") @NotNull MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory) {
        if (file.isEmpty()) {
            throw new StorageException("请选择需要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new StorageException("图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new StorageException("仅支持上传图片文件");
        }

        String baseDir = StringUtils.hasText(directory)
                ? directory.trim()
                : "images/" + LocalDate.now().format(PATH_FORMAT);

        try (var inputStream = file.getInputStream()) {
            StoredObjectInfo stored = fileStorageService.upload(
                    baseDir,
                    file.getOriginalFilename(),
                    inputStream,
                    file.getSize(),
                    contentType);

            UploadResponse response = UploadResponse.builder()
                    .bucket(stored.bucket())
                    .objectKey(stored.objectKey())
                    .url(stored.url())
                    .fileName(file.getOriginalFilename())
                    .size(file.getSize())
                    .contentType(contentType)
                    .build();
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new StorageException("上传图片失败，请稍后再试。", e);
        }
    }
}
