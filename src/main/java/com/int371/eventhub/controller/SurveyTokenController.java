package com.int371.eventhub.controller;

import com.int371.eventhub.dto.ApiResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "ลิงก์แบบสำรวจไม่ถูกต้อง", null));
            }

            // 2. ตรวจสอบสถานะการใช้งานและวันหมดอายุ
            if (surveyToken.isUsed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "แบบสำรวจนี้ได้ถูกส่งไปแล้ว", null));
            }

            if (surveyToken.getExpiryDate() != null && surveyToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.GONE)
                    .body(new ApiResponse<>(HttpStatus.GONE.value(), "ลิงก์นี้หมดอายุแล้ว", null));
            }

            // 3. ตรวจสอบความสมบูรณ์ของข้อมูล
            if (surveyToken.getUser() == null || surveyToken.getEvent() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ข้อมูลในระบบไม่สมบูรณ์", null));
            }

            // 4. ดึง Role และสร้าง Access Token
            Optional<MemberEvent> memEventRole = mem.findByUserIdAndEventId(
                    surveyToken.getUser().getId(), 
                    surveyToken.getEvent().getId()
            );

            if (memEventRole.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "คุณไม่มีสิทธิ์เข้าถึงแบบสำรวจนี้", null));
            }

            Optional<Event> event = eventRepository.findById(surveyToken.getEvent().getId());
            if (event.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "ไม่พบข้อมูลกิจกรรม", null));
            }

            // สร้าง Restricted Token (SURVEY_GUEST)
            String accessToken = jwtService.generateSurveyToken(surveyToken.getUser());

            // 5. เตรียมข้อมูล DTO
            SurveyVerifyResponseDto data = new SurveyVerifyResponseDto(
                event.get().getId(),                     
                event.get().getEventName(),               
                surveyToken.getUser().getId(), 
                surveyToken.getUser().getFirstName(), 
                surveyToken.getUser().getLastName(), 
                memEventRole.get().getEventRole().toString(),
                accessToken
            );

            // 6. ส่งกลับในรูปแบบ ApiResponse
            ApiResponse<SurveyVerifyResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "ตรวจสอบสถานะลิงก์สำเร็จ",
                data
            );
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "เกิดข้อผิดพลาด: " + e.getMessage(), null));
        }
    }
}