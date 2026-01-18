package com.int371.eventhub.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.CreateSurveyRequestDto;
import com.int371.eventhub.dto.SurveyGroupResponseDto;
import com.int371.eventhub.dto.SurveyResponseDto;
import com.int371.eventhub.service.SurveyService;

@RestController
@RequestMapping("/events/{eventId}/surveys")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @GetMapping("/pre")
    public ResponseEntity<ApiResponse<SurveyGroupResponseDto>> getPreSurveys(@PathVariable Integer eventId) {
        SurveyGroupResponseDto data = surveyService.getPreSurveys(eventId);
        
        ApiResponse<SurveyGroupResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Pre-surveys fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post")
    public ResponseEntity<ApiResponse<SurveyGroupResponseDto>> getPostSurveys(@PathVariable Integer eventId) {
        SurveyGroupResponseDto data = surveyService.getPostSurveys(eventId);
        
        ApiResponse<SurveyGroupResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Post-surveys fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("") // POST /events/{eventId}/surveys
    public ResponseEntity<ApiResponse<SurveyResponseDto>> createSurvey(
            @PathVariable Integer eventId,
            @RequestBody CreateSurveyRequestDto request,
            Principal principal) { // Principal จะเก็บ User ที่ Login อยู่ (จาก JWT)

        // principal.getName() จะได้ email ของ user
        SurveyResponseDto createdSurvey = surveyService.createSurvey(eventId, request, principal.getName());

        ApiResponse<SurveyResponseDto> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Survey created successfully",
                createdSurvey
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}