package com.erokin.campusclubmanagement.service;

public interface QrCodeService {

    /**
     * Generates a PNG image representing the given text in QR code format.
     *
     * @param text content encoded in the QR code
     * @param size output image width/height in pixels
     * @return byte array of the PNG image
     */
    byte[] generatePng(String text, int size);
}
