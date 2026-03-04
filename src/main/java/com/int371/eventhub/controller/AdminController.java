package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.AdminAddUserToEventRequestDto;
import com.int371.eventhub.dto.AdminUpdateUserRoleInEventRequestDto;
import com.int371.eventhub.dto.AdminCreateUserRequestDto;
import com.int371.eventhub.dto.AdminCreateUserResponseDto;
import com.int371.eventhub.dto.AdminUpdateUserRequestDto;
import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.service.AdminService;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.Map;
import java.util.List;

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

        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
                List<Map<String, Object>> users = adminService.getAllUsers();

                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all users successfully by admin.",
                                users);

                return ResponseEntity.ok(response);
        }

        @PutMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(
                        @PathVariable Integer userId,
                        @Valid @RequestBody AdminUpdateUserRequestDto request) {

                Map<String, Object> userData = adminService.updateUser(userId, request);

                ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "User updated successfully by admin.",
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
                                "User deleted successfully by admin.",
                                null);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/events/{eventId}/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> addUserToEvent(
                        @PathVariable Integer eventId,
                        @Valid @RequestBody AdminAddUserToEventRequestDto request) {

                Map<String, Object> result = adminService.addUserToEvent(eventId, request);

                ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                                HttpStatus.CREATED.value(),
                                "User added to event successfully by admin.",
                                result);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @DeleteMapping("/events/{eventId}/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> removeUserFromEvent(
                        @PathVariable Integer eventId,
                        @PathVariable Integer userId) {

                adminService.removeUserFromEvent(eventId, userId);

                ApiResponse<Void> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "User removed from event successfully by admin.",
                                null);

                return ResponseEntity.ok(response);
        }

        @PutMapping("/events/{eventId}/users/{userId}/role")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> updateMemberEventRole(
                        @PathVariable Integer eventId,
                        @PathVariable Integer userId,
                        @Valid @RequestBody AdminUpdateUserRoleInEventRequestDto request) {

                Map<String, Object> result = adminService.updateMemberEventRole(eventId, userId, request.getRole());

                ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "User role in event updated successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllEventMembers() {

                List<Map<String, Object>> result = adminService.getAllEventMembers();

                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all users across all events successfully.",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/{eventId}/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsersInEvent(
                        @PathVariable Integer eventId) {

                List<Map<String, Object>> result = adminService.getAllUsersInEvent(eventId);

                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all users in event successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }
}
