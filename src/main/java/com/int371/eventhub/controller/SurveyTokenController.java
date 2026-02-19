package com.int371.eventhub.controller;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.SurveyVerifyResponseDto;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.ResponseAnswerRepository;
import com.int371.eventhub.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/surveys")
public class SurveyTokenController {

        @Autowired
        private JwtService jwtService;

        @Autowired
        private MemberEventRepository memberEventRepository;

        @Autowired
        private ResponseAnswerRepository responseAnswerRepository;

        @GetMapping("/verify")
        @Transactional(readOnly = true)
        public ResponseEntity<?> verifyToken(HttpServletRequest request) {
                try {

                        // 1️⃣ ดึง Token
                        String authHeader = request.getHeader("Authorization");
                        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(new ApiResponse<>(401, "ไม่พบ token", null));
                        }

                        String token = authHeader.substring(7);

                        // 2️⃣ Extract Claims
                        Claims claims = jwtService.extractAllClaims(token);

                        Integer userId = claims.get("userId", Integer.class);
                        Integer eventId = claims.get("eventId", Integer.class);
                        String roleInEvent = claims.get("roleInEvent", String.class);
                        String tokenRole = claims.get("tokenRole", String.class);

                        System.out.printf("claims", claims);

                        // 3️⃣ ตรวจประเภท token (Allow ONLY SURVEY_GUEST)
                        if (!"SURVEY_GUEST".equals(tokenRole)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(new ApiResponse<>(403,
                                                                "ไม่อนุญาตให้ใช้ Token นี้ (ต้องเป็น Survey Link เท่านั้น)",
                                                                null));
                        }

                        // 4️⃣ หา MemberEvent
                        Optional<MemberEvent> optionalMember = memberEventRepository.findByUserIdAndEventId(
                                        userId,
                                        eventId);

                        if (optionalMember.isEmpty()) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(new ApiResponse<>(404, "ไม่พบข้อมูลผู้เข้าร่วมงาน", claims));
                        }

                        MemberEvent memberEvent = optionalMember.get();

                        // 5️⃣ เช็คว่าตอบแล้วหรือยัง
                        boolean alreadyAnswered = responseAnswerRepository.existsByMemberEventId(memberEvent.getId());

                        if (alreadyAnswered) {
                                return ResponseEntity.status(HttpStatus.CONFLICT)
                                                .body(new ApiResponse<>(
                                                                409,
                                                                "แบบสอบถามนี้ได้ถูกทำไปแล้ว",
                                                                null));
                        }

                        // 6️⃣ สร้าง Response DTO
                        SurveyVerifyResponseDto data = new SurveyVerifyResponseDto();
                        // data.setEventId(eventId);
                        // data.setUserId(userId);
                        data.setEventRole(roleInEvent);
                        // data.setAccessToken(token);

                        return ResponseEntity.ok(
                                        new ApiResponse<>(200, "ตรวจสอบสถานะลิงก์สำเร็จ", data));

                } catch (ExpiredJwtException e) {
                        return ResponseEntity.status(HttpStatus.GONE)
                                        .body(new ApiResponse<>(410, "ลิงก์นี้หมดอายุแล้ว", null));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ApiResponse<>(400, "ลิงก์ไม่ถูกต้อง", null));
                }
        }

}