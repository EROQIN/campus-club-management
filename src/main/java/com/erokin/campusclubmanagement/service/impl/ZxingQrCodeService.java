package com.erokin.campusclubmanagement.service.impl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.erokin.campusclubmanagement.exception.StorageException;
import com.erokin.campusclubmanagement.service.QrCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

@Service
public class ZxingQrCodeService implements QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(ZxingQrCodeService.class);

    @Override
    public byte[] generatePng(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", bos);
                return bos.toByteArray();
            }
        } catch (WriterException e) {
            log.error("Failed to generate QR code", e);
            throw new StorageException("生成二维码失败，请稍后再试。", e);
        } catch (Exception e) {
            log.error("Unexpected error while generating QR code", e);
            throw new StorageException("生成二维码失败，请稍后再试。", e);
        }
    }
}
