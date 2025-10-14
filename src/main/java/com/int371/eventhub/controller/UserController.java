package com.int371.eventhub.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.UserProfileDto;
import com.int371.eventhub.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile(Principal principal) {
        String userEmail = principal.getName();
        UserProfileDto userProfile = userService.getUserProfile(userEmail);

        ApiResponse<UserProfileDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User profile fetched successfully",
                userProfile
        );
        return ResponseEntity.ok(response);
    }
}