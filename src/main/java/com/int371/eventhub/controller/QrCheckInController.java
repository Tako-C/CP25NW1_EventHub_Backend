package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.CheckInRequestDto;
import com.int371.eventhub.service.EventRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/qr")
public class QrCheckInController {

    @Autowired
    private EventRegistrationService eventRegistrationService;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<String>> checkInUser(@Valid @RequestBody CheckInRequestDto request) {
        
        String resultMessage = eventRegistrationService.checkInUser(request);

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                resultMessage,
                "Checked In"
        );
        return ResponseEntity.ok(response);
    }
}