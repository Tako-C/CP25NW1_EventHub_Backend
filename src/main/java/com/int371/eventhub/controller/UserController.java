package com.int371.eventhub.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.EditUserProfileRequestDto;
import com.int371.eventhub.dto.RegisteredEventDto;
import com.int371.eventhub.dto.UserProfileDto;
import com.int371.eventhub.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/users")
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

    @GetMapping("/me/registered-events")
    public ResponseEntity<ApiResponse<List<RegisteredEventDto>>> getMyRegisteredEvents(Principal principal) {
        String userEmail = principal.getName();
        List<RegisteredEventDto> events = userService.getRegisteredEvents(userEmail);

        String message;
        if (events.isEmpty()) {
            message = "You have not registered for any events yet.";
        } else {
            message = "Registered events fetched successfully";
        }

        ApiResponse<List<RegisteredEventDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                events
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditUserProfileRequestDto>> editUserProfile(@RequestBody EditUserProfileRequestDto editRequest) {
        EditUserProfileRequestDto updatedProfile = userService.editUserProfile(editRequest);

        ApiResponse<EditUserProfileRequestDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User profile updated successfully",
                updatedProfile
        );
        return ResponseEntity.ok(response);
    }
    
}