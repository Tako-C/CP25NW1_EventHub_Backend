package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.CheckInPreviewResponseDto;
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

    @PostMapping("/check-in")
    public GenericResponse<CheckInResponseDto> checkInUser(@Valid @RequestBody CheckInRequestDto request) {
        return eventRegistrationService.checkInUser(request);   
    }

    @PostMapping("/user-info")
    public ResponseEntity<ApiResponse<CheckInPreviewResponseDto>> getUserInfoFromQr(@Valid @RequestBody CheckInRequestDto request) {
        
        CheckInPreviewResponseDto previewData = eventRegistrationService.getCheckInPreview(request);

        ApiResponse<CheckInPreviewResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Preview info fetched successfully",
                previewData
        );
        return ResponseEntity.ok(response);
    }
}