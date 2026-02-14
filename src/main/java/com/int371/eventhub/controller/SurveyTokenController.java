package com.int371.eventhub.controller;

import com.int371.eventhub.dto.SurveyVerifyResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.SurveyToken;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.SurveyTokenRepository;
import com.int371.eventhub.service.JwtService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/surveys")
public class SurveyTokenController {

    @Autowired
    private SurveyTokenRepository surveyTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MemberEventRepository mem;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/verify")
    @Transactional(readOnly = true)
    public ResponseEntity<?> verifyToken(@RequestParam("t") String token) {
        try {
            // 1. ค้นหา Token
            SurveyToken surveyToken = surveyTokenRepository.findById(token).orElse(null);

            if (surveyToken == null) {
                return ResponseEntity.status(404).body(Map.of("message", "ลิงก์แบบสำรวจไม่ถูกต้อง"));
            }

            // 2. ตรวจสอบสถานะการใช้งานและวันหมดอายุ
            if (surveyToken.isUsed()) {
                return ResponseEntity.status(400).body(Map.of("message", "แบบสำรวจนี้ได้ถูกส่งไปแล้ว"));
            }

            if (surveyToken.getExpiryDate() != null && surveyToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(410).body(Map.of("message", "ลิงก์นี้หมดอายุแล้ว"));
            }

            // 3. ตรวจสอบว่า User และ Event ใน Token ไม่เป็น Null
            if (surveyToken.getUser() == null || surveyToken.getEvent() == null) {
                return ResponseEntity.status(500).body(Map.of("message", "ข้อมูลในระบบไม่สมบูรณ์ (ไม่พบผู้ใช้หรือกิจกรรม)"));
            }

            // 4. ดึง Role และสร้าง Access Token
            Optional<MemberEvent> memEventRole = mem.findByUserIdAndEventId(
                    surveyToken.getUser().getId(), 
                    surveyToken.getEvent().getId()
            );

            if (memEventRole == null || !memEventRole.isPresent()) {
                return ResponseEntity.status(403).body(Map.of("message", "คุณไม่มีสิทธิ์เข้าถึงแบบสำรวจนี้"));
            }

            Optional<Event> event = eventRepository.findById(surveyToken.getEvent().getId());

            if (!event.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("message", "ไม่พบข้อมูลกิจกรรม"));
            }

            String accessToken = jwtService.generateSurveyToken(surveyToken.getUser());

            // 5. สร้าง DTO และส่งข้อมูลกลับ
            SurveyVerifyResponseDto response = new SurveyVerifyResponseDto(
                event.get().getId(),                     
                event.get().getEventName(),               
                surveyToken.getUser().getId(), 
                surveyToken.getUser().getFirstName(), 
                surveyToken.getUser().getLastName(), 
                memEventRole.get().getEventRole().toString(),  // แปลง Role เป็น String
                accessToken
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of(
                "message", "เกิดข้อผิดพลาดภายในระบบ",
                "error", e.getMessage()
            ));
        }
    }
}