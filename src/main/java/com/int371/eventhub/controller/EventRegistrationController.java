package com.int371.eventhub.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.EventRegisterRequestDto;
import com.int371.eventhub.dto.EventRegisterResponseDto;
import com.int371.eventhub.dto.LoginOtpAndEventRegisterVerifyRequestDto;
import com.int371.eventhub.service.EventRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events/{eventId}/register") 
public class EventRegistrationController {
    
    @Autowired
    private EventRegistrationService eventRegistrationService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventRegisterResponseDto>> registerAsLoggedInUser(
            @PathVariable Integer eventId, 
            Principal principal) {

        String userEmail = principal.getName();
        

        String qrCodeUrl = eventRegistrationService.registerAuthenticatedUser(eventId, userEmail);

        EventRegisterResponseDto data = EventRegisterResponseDto.builder()
                .qrCodeUrl(qrCodeUrl)
                .build();

        ApiResponse<EventRegisterResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Event registration successful.",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<?>> requestOtp(
            @PathVariable Integer eventId, 
            @Valid @RequestBody EventRegisterRequestDto request) {
        
        String message = eventRegistrationService.requestEventOtp(eventId, request);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                request.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<EventRegisterResponseDto>> verifyOtpAndRegister(
            @PathVariable Integer eventId,
            @Valid @RequestBody LoginOtpAndEventRegisterVerifyRequestDto request) {
        
        EventRegisterResponseDto serviceResult = eventRegistrationService.verifyOtpAndRegister(eventId, request);
        
        EventRegisterResponseDto data = EventRegisterResponseDto.builder()
                .token(serviceResult.getToken())
                .qrCodeUrl(serviceResult.getQrCodeUrl())
                .build();

        ApiResponse<EventRegisterResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                serviceResult.getMessage(),
                data
        );
        return ResponseEntity.ok(response);
    }
}