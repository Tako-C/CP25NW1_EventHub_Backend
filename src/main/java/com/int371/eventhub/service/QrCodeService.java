package com.int371.eventhub.service;

import java.awt.image.BufferedImage;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

    private final ObjectMapper objectMapper;

    public QrCodeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("UseSpecificCatch")
    public BufferedImage generateQrCodeImage(Object data, int width, int height) {
        try {
            String jsonContent = objectMapper.writeValueAsString(data);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonContent, BarcodeFormat.QR_CODE, width, height);

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR Code image", e);
        }
    }
}