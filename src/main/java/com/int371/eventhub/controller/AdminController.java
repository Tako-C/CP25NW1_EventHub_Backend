package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.AdminCreateUserRequestDto;
import com.int371.eventhub.dto.AdminCreateUserResponseDto;
import com.int371.eventhub.dto.AdminUpdateUserRequestDto;
import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.service.AdminService;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminCreateUserResponseDto>> createUser(
            @Valid @RequestBody AdminCreateUserRequestDto request,
            Principal principal) {

        AdminCreateUserResponseDto responseDto = adminService.createUserByAdmin(request, principal.getName());

        ApiResponse<AdminCreateUserResponseDto> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "User created successfully by admin.",
                responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUpdateUserRequestDto request) {

        Map<String, Object> userData = adminService.updateUser(userId, request);

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User updated successfully.",
                userData);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Integer userId) {

        adminService.deleteUser(userId);

        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User deleted successfully.",
                null);

        return ResponseEntity.ok(response);
    }
}
