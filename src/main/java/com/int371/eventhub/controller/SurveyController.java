package com.int371.eventhub.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.CreateSurveyRequestDto;
import com.int371.eventhub.dto.SurveyGroupResponseDto;
import com.int371.eventhub.dto.SurveyResponseDto;
import com.int371.eventhub.dto.SurveyResponseSubmissionStatusDto;
import com.int371.eventhub.dto.UpdateSurveyRequestDto;
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
                                data);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/post")
        public ResponseEntity<ApiResponse<SurveyGroupResponseDto>> getPostSurveys(@PathVariable Integer eventId) {
                SurveyGroupResponseDto data = surveyService.getPostSurveys(eventId);

                ApiResponse<SurveyGroupResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Post-surveys fetched successfully",
                                data);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{surveyId}/submission-status")
        public ResponseEntity<List<SurveyResponseSubmissionStatusDto>> getSurveySubmissionStatus(
                        @PathVariable Integer eventId,
                        @PathVariable Integer surveyId,
                        Principal principal) {

                return ResponseEntity.ok(
                                surveyService.getSurveySubmissionStatus(
                                                eventId, surveyId, principal.getName()));
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
                                createdSurvey);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping("/{surveyId}")
        public ResponseEntity<ApiResponse<SurveyResponseDto>> updateSurvey(
                        @PathVariable Integer eventId,
                        @PathVariable Integer surveyId,
                        @RequestBody UpdateSurveyRequestDto request,
                        Principal principal) {

                SurveyResponseDto updatedSurvey = surveyService.updateSurvey(eventId, surveyId, request,
                                principal.getName());

                ApiResponse<SurveyResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Survey updated successfully",
                                updatedSurvey);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{surveyId}")
        public ResponseEntity<ApiResponse<String>> deleteSurvey(
                        @PathVariable Integer eventId,
                        @PathVariable Integer surveyId,
                        Principal principal) {

                surveyService.deleteSurvey(eventId, surveyId, principal.getName());

                ApiResponse<String> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Survey deleted successfully",
                                null);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/answers")
        public ResponseEntity<ApiResponse<String>> submitSurveyAnswers(
                        @PathVariable Integer eventId,
                        @RequestBody com.int371.eventhub.dto.SurveySubmissionRequestDto request,
                        Principal principal) {

                surveyService.submitSurveyAnswers(eventId, request, principal.getName());

                ApiResponse<String> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Survey answers submitted successfully",
                                null);
                return ResponseEntity.ok(response);
        }
}