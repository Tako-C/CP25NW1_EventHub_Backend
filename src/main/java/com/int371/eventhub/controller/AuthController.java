package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.JwtResponse;
import com.int371.eventhub.dto.LoginOtpRequest;
import com.int371.eventhub.dto.LoginOtpVerificationRequest;
import com.int371.eventhub.dto.LoginRequest;
import com.int371.eventhub.dto.RegisterOtpRequest;
import com.int371.eventhub.dto.RegisterOtpVerificationRequest;
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
    public ResponseEntity<ApiResponse<?>> requestOtp(@Valid @RequestBody RegisterOtpRequest otpRequest) {
        otpService.generateAndSendOtp(otpRequest);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "OTP has been sent to your email.",
                otpRequest.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/otp/verify")
    public ResponseEntity<ApiResponse<String>> verifyOtpAndRegister(@Valid @RequestBody RegisterOtpVerificationRequest verificationRequest) {
        User registeredUser = authService.registerWithOtp(verificationRequest);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User registered successfully via OTP!",
                registeredUser.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        ApiResponse<JwtResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Login successful!",
                new JwtResponse(token)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/otp/request")
    public ResponseEntity<ApiResponse<?>> requestLoginOtp(@Valid @RequestBody LoginOtpRequest request) {
        otpService.generateAndSendLoginOtp(request.getEmail());
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "A login OTP has been sent to your email.",
                request.getEmail()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/otp/verify")
    public ResponseEntity<ApiResponse<JwtResponse>> verifyLoginOtp(@Valid @RequestBody LoginOtpVerificationRequest request) {
        String token = authService.loginWithOtp(request);
        ApiResponse<JwtResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Login successful!",
                new JwtResponse(token)
        );
        return ResponseEntity.ok(response);
    }
}