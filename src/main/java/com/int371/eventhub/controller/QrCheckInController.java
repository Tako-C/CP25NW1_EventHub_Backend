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
import com.int371.eventhub.dto.CheckInResponseDto;
import com.int371.eventhub.dto.GenericResponse;
import com.int371.eventhub.service.EventRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/qr")
public class QrCheckInController {

    @Autowired
    private EventRegistrationService eventRegistrationService;

    // ใน EventRegistrationController.java

    @PostMapping("/check-in")
    public GenericResponse<CheckInResponseDto> checkInUser(@Valid @RequestBody CheckInRequestDto request) {
        return eventRegistrationService.checkInUser(request);   
    }
}