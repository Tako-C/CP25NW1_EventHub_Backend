package com.int371.eventhub.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.security.Principal;

import javax.imageio.ImageIO;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.PocQrCodeData;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.repository.UserRepository;
import com.int371.eventhub.service.QrCodeService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final UserRepository userRepository;

    public QrCodeController(QrCodeService qrCodeService, UserRepository userRepository) {
        this.qrCodeService = qrCodeService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint สำหรับให้ User ที่ Login แล้ว ดึง QR Code ของตัวเอง (สำหรับ POC)
     *
     * @param principal ข้อมูล User ที่ Login อยู่ (ได้จาก Spring Security)
     * @param response  สำหรับเขียนรูปภาพ PNG กลับไป
     */
    @GetMapping("/me")
    public void getMyPocQrCode(Principal principal, HttpServletResponse response) {
        try {
            // 1. ดึง Email ของ User ที่ Login อยู่
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            // 2. สร้าง Data Object ที่จะฝังใน QR Code
            PocQrCodeData qrData = PocQrCodeData.builder()
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .roleId(user.getRole().getId())
                    .build();

            // 3. เรียก Service ให้สร้างรูปภาพ QR Code
            // (ขนาด 250x250 pixels)
            BufferedImage qrImage = qrCodeService.generatePocQrCodeImage(qrData, 250, 250);

            // 4. ตั้งค่า Response Header ให้เป็น image/png
            response.setContentType("image/png");

            // 5. เขียนรูปภาพ (BufferedImage) ลงใน Response Output Stream
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(qrImage, "png", outputStream);
            outputStream.close();

        } catch (Exception e) {
            // ถ้าเกิดข้อผิดพลาด (เช่น หา User ไม่เจอ หรือสร้างรูปไม่สำเร็จ)
            // เราสามารถตั้งค่า Error Response ได้
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // ควรมี Log Error ไว้ด้วย
            e.printStackTrace();
        }
    }
}