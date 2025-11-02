package com.int371.eventhub.service;

import java.awt.image.BufferedImage;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.int371.eventhub.dto.PocQrCodeData;

@Service
public class QrCodeService {

    private final ObjectMapper objectMapper;

    // เราใช้ ObjectMapper เพื่อแปลง Object เป็น JSON String
    public QrCodeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * สร้าง QR Code สำหรับ POC โดยใช้ข้อมูล User
     *
     * @param data ข้อมูล User ที่จะฝังใน QR Code
     * @param width ความกว้างของรูปภาพ QR Code
     * @param height ความสูงของรูปภาพ QR Code
     * @return BufferedImage ของ QR Code
     */
    public BufferedImage generatePocQrCodeImage(PocQrCodeData data, int width, int height) {
        try {
            // 1. แปลง Data Object เป็น JSON String
            String jsonContent = objectMapper.writeValueAsString(data);

            // 2. สร้าง QR Code จาก JSON String
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonContent, BarcodeFormat.QR_CODE, width, height);

            // 3. แปลง BitMatrix (ข้อมูล QR) เป็น BufferedImage (รูปภาพ)
            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (Exception e) {
            // ในระบบจริง ควรจัดการ Exception ให้ดีกว่านี้
            throw new RuntimeException("Failed to generate QR Code image", e);
        }
    }
}