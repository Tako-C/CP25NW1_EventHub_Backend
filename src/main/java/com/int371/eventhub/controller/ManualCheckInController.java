package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.ManualCheckInRequestDto;
import com.int371.eventhub.dto.SearchUserCheckInRequestDto;
import com.int371.eventhub.dto.SearchUserCheckInResponseDto;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.service.EventRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/manual")
public class ManualCheckInController {
    @Autowired
    private EventRegistrationService eventRegistrationService;

    @PostMapping("/search")
    public ResponseEntity<?> searchUserCheckIn(@RequestBody SearchUserCheckInRequestDto request) {
        try {
            SearchUserCheckInResponseDto response = eventRegistrationService.searchUser(request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            ApiResponse<String> apiResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<String>> checkInUser(@Valid @RequestBody ManualCheckInRequestDto request) {
        
        String resultMessage = eventRegistrationService.manualCheckInUser(request);

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                resultMessage,
                null
        );
        return ResponseEntity.ok(response);
    }

}
