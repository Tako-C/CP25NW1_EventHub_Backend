package com.int371.eventhub.controller;

import com.int371.eventhub.entity.SurveyToken;
import com.int371.eventhub.repository.SurveyTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/surveys")
public class SurveyTokenController {

    @Autowired
    private SurveyTokenRepository surveyTokenRepository;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam("t") String token) {
        // 1. ค้นหา Token ในฐานข้อมูล
        SurveyToken surveyToken = surveyTokenRepository.findById(token).orElse(null);

        // 2. ตรวจสอบว่ามี Token นี้อยู่จริงไหม
        if (surveyToken == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Invalid survey link"));
        }

        // 3. ตรวจสอบว่าเคยถูกใช้ไปแล้วหรือยัง
        if (surveyToken.isUsed()) {
            return ResponseEntity.status(400).body(Map.of("message", "This survey has already been submitted"));
        }

        // 4. ตรวจสอบวันหมดอายุ
        if (surveyToken.getExpiryDate() != null && surveyToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(410).body(Map.of("message", "This link has expired"));
        }

        // 5. ถ้าผ่านทุกเงื่อนไข ให้ส่งข้อมูลที่จำเป็นกลับไปให้หน้าบ้าน (เช่น ชื่อ Event หรือประเภทสมาชิก)
        return ResponseEntity.ok(Map.of(
                "eventName", surveyToken.getEvent().getEventName(),
                "eventId", surveyToken.getEvent().getId(),
                "firstName", surveyToken.getUser().getFirstName()
        ));
    }
}