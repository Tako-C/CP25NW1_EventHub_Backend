package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.OtpRequest;
import com.int371.eventhub.dto.OtpVerificationRequest;
import com.int371.eventhub.dto.RegisterRequest;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.service.AuthService;
import com.int371.eventhub.service.OtpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User registeredUser = authService.register(registerRequest);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User registered successfully!",
                registeredUser.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<?>> requestOtp(@Valid @RequestBody OtpRequest otpRequest) {
        otpService.generateAndSendOtp(otpRequest.getEmail());
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "OTP has been sent to your email.",
                otpRequest.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<String>> verifyOtpAndRegister(@Valid @RequestBody OtpVerificationRequest verificationRequest) {
        User registeredUser = otpService.verifyOtpAndRegister(verificationRequest);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User registered successfully via OTP!",
                registeredUser.getEmail()
        );
        return ResponseEntity.ok(response);
    }
}