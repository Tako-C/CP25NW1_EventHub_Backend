package com.int371.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.int371.eventhub.dto.AdminAddUserToEventRequestDto;
import com.int371.eventhub.dto.AdminUpdateUserRoleInEventRequestDto;
import com.int371.eventhub.dto.AdminCreateUserRequestDto;
import com.int371.eventhub.dto.AdminCreateUserResponseDto;
import com.int371.eventhub.dto.AdminUpdateUserRequestDto;
import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.service.AdminService;
import com.int371.eventhub.service.EventRewardService;
import com.int371.eventhub.service.EventService;
import com.int371.eventhub.service.SurveyService;
import com.int371.eventhub.dto.EditEventRequestDto;
import com.int371.eventhub.dto.EventRequestDto;
import com.int371.eventhub.dto.UpdateSurveyRequestDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.dto.CreateEventRewardRequestDto;
import com.int371.eventhub.dto.CreateSurveyRequestDto;
import com.int371.eventhub.dto.EventResponseDto;
import com.int371.eventhub.dto.SurveyResponseDto;
import com.int371.eventhub.dto.EventRewardResponseDto;
import com.int371.eventhub.dto.AdminBulkImportResponseDto;
import com.int371.eventhub.service.AdminBulkImportService;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

        @Autowired
        private AdminService adminService;

        @Autowired
        private AdminBulkImportService adminBulkImportService;

        @Autowired
        private EventService eventService;

        @Autowired
        private SurveyService surveyService;

        @Autowired
        private EventRewardService eventRewardService;

        @Autowired
        private MemberEventRepository memberEventRepository;

        private String checkAdminOrOrganizer(Integer eventId, Principal principal) {
                if (principal == null) {
                        throw new AccessDeniedException("Authentication required.");
                }

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                                || a.getAuthority().equals("ADMIN"));
                if (isAdmin) {
                        return "admin";
                }

                MemberEvent me = memberEventRepository.findByUserEmailAndEventId(principal.getName(), eventId)
                                .orElseThrow(() -> new AccessDeniedException("Access denied."));

                if (me.getEventRole() != MemberEventRole.ORGANIZER) {
                        throw new AccessDeniedException(
                                        "Access denied. Only the organizer or admin can perform this action.");
                }

                return "organizer";
        }

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
        @PreAuthorize("hasAnyRole('ADMIN', 'GENERAL_USER')")
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
        @PreAuthorize("hasAnyRole('ADMIN', 'GENERAL_USER')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> addUserToEvent(
                        @PathVariable Integer eventId,
                        @Valid @RequestBody AdminAddUserToEventRequestDto request,
                        Principal principal) {

                String actor = checkAdminOrOrganizer(eventId, principal);

                Map<String, Object> result = adminService.addUserToEvent(eventId, request);

                ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                                HttpStatus.CREATED.value(),
                                "User added to event successfully by " + actor + ".",
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
        @PreAuthorize("hasAnyRole('ADMIN', 'GENERAL_USER')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> updateMemberEventRole(
                        @PathVariable Integer eventId,
                        @PathVariable Integer userId,
                        @Valid @RequestBody AdminUpdateUserRoleInEventRequestDto request,
                        Principal principal) {

                String actor = checkAdminOrOrganizer(eventId, principal);

                Map<String, Object> result = adminService.updateMemberEventRole(eventId, userId, request.getRole());

                ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "User role in event updated successfully by " + actor + ".",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/users")
        @PreAuthorize("hasAnyRole('ADMIN', 'GENERAL_USER')")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllEventMembers() {

                List<Map<String, Object>> result = adminService.getAllEventMembers();

                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all users across all events successfully.",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/{eventId}/users")
        @PreAuthorize("hasAnyRole('ADMIN', 'GENERAL_USER')")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsersInEvent(
                        @PathVariable Integer eventId, Principal principal) {

                String actor = checkAdminOrOrganizer(eventId, principal);

                List<Map<String, Object>> result = adminService.getAllUsersInEvent(eventId);

                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all users in event successfully by" + actor + ".",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAllEventsForAdmin() {

                List<EventResponseDto> result = eventService.getAllEventsForAdmin();

                ApiResponse<List<EventResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all events successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/{eventId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EventResponseDto>> getEventByIdForAdmin(
                        @PathVariable Integer eventId) {

                EventResponseDto result = eventService.getEventByIdForAdmin(eventId);

                ApiResponse<EventResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched event successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @PutMapping("/events/{eventId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EventResponseDto>> updateEvent(
                        @PathVariable Integer eventId,
                        @Valid @ModelAttribute EditEventRequestDto request) {

                Event updatedEvent = eventService.updateEventForAdmin(eventId, request);

                ApiResponse<EventResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Event updated successfully by admin.",
                                eventService.getEventByIdForAdmin(updatedEvent.getId()));

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/{eventId}/surveys")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<SurveyResponseDto>>> getAllSurveysByEventIdForAdmin(
                        @PathVariable Integer eventId) {

                List<SurveyResponseDto> result = surveyService.getAllSurveysByEventIdForAdmin(eventId);

                ApiResponse<List<SurveyResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all surveys for event successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/surveys")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<SurveyResponseDto>>> getAllSurveysForAdmin() {

                List<SurveyResponseDto> result = surveyService.getAllSurveysForAdmin();

                ApiResponse<List<SurveyResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all surveys successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @PutMapping("/events/{eventId}/surveys/{surveyId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<SurveyResponseDto>> updateSurvey(
                        @PathVariable Integer eventId,
                        @PathVariable Integer surveyId,
                        @RequestBody UpdateSurveyRequestDto request) {

                SurveyResponseDto updatedSurvey = surveyService.updateSurveyForAdmin(eventId, surveyId, request);

                ApiResponse<SurveyResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Survey updated successfully by admin.",
                                updatedSurvey);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/events/{eventId}/rewards")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<EventRewardResponseDto>>> getAllRewardsByEventIdForAdmin(
                        @PathVariable Integer eventId) {

                List<EventRewardResponseDto> result = eventRewardService.getAllRewardsByEventIdForAdmin(eventId);

                ApiResponse<List<EventRewardResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all rewards for event successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/rewards")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<EventRewardResponseDto>>> getAllRewardsForAdmin() {

                List<EventRewardResponseDto> result = eventRewardService.getAllRewardsForAdmin();

                ApiResponse<List<EventRewardResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Fetched all rewards successfully by admin.",
                                result);

                return ResponseEntity.ok(response);
        }

        @PutMapping(value = "/events/{eventId}/rewards/{rewardId}", consumes = { "multipart/form-data" })
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EventRewardResponseDto>> updateReward(
                        @PathVariable Integer eventId,
                        @PathVariable Integer rewardId,
                        @ModelAttribute CreateEventRewardRequestDto request) {

                EventRewardResponseDto updatedReward = eventRewardService.updateRewardForAdmin(eventId, rewardId,
                                request);

                ApiResponse<EventRewardResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Reward updated successfully by admin.",
                                updatedReward);

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/events/{eventId}/hard-delete")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> hardDeleteEvent(
                        @PathVariable Integer eventId) {

                eventService.hardDeleteEventForAdmin(eventId);

                ApiResponse<Void> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Event hard deleted successfully by admin.",
                                null);

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/events/{eventId}/surveys/{surveyId}/hard-delete")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> hardDeleteSurvey(
                        @PathVariable Integer eventId,
                        @PathVariable Integer surveyId) {

                surveyService.hardDeleteSurveyForAdmin(eventId, surveyId);

                ApiResponse<Void> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Survey hard deleted successfully by admin.",
                                null);

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/events/{eventId}/rewards/{rewardId}/hard-delete")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> hardDeleteReward(
                        @PathVariable Integer eventId,
                        @PathVariable Integer rewardId) {

                eventRewardService.hardDeleteRewardForAdmin(eventId, rewardId);

                ApiResponse<Void> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Reward hard deleted successfully by admin.",
                                null);

                return ResponseEntity.ok(response);
        }

        @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(
                        @Valid @ModelAttribute EventRequestDto dto,
                        Principal principal) {

                EventResponseDto createdEvent = eventService.createEventForAdmin(dto, principal.getName());

                ApiResponse<EventResponseDto> response = new ApiResponse<>(
                                HttpStatus.CREATED.value(),
                                "Event created successfully by admin.",
                                createdEvent);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping("/events/{eventId}/surveys")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<SurveyResponseDto>> createSurvey(
                        @PathVariable Integer eventId,
                        @RequestBody CreateSurveyRequestDto request) {

                SurveyResponseDto createdSurvey = surveyService.createSurveyForAdmin(eventId, request);

                ApiResponse<SurveyResponseDto> response = new ApiResponse<>(
                                HttpStatus.CREATED.value(),
                                "Survey created successfully by admin.",
                                createdSurvey);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping(value = "/events/{eventId}/rewards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<EventRewardResponseDto>> createReward(
                        @PathVariable Integer eventId,
                        @ModelAttribute CreateEventRewardRequestDto request) {

                EventRewardResponseDto createdReward = eventRewardService.createRewardForAdmin(eventId, request);

                ApiResponse<EventRewardResponseDto> response = new ApiResponse<>(
                                HttpStatus.CREATED.value(),
                                "Reward created successfully by admin.",
                                createdReward);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping(value = "/events/{eventId}/users/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ADMIN', 'GENERAL_USER')")
        public ResponseEntity<ApiResponse<AdminBulkImportResponseDto>> bulkImportUsers(
                        @PathVariable Integer eventId,
                        @RequestParam("file") MultipartFile file,
                        Principal principal) {

                checkAdminOrOrganizer(eventId, principal);

                AdminBulkImportResponseDto result = adminBulkImportService.importUsers(eventId, file);

                if (result.getSuccessCount() == 0 && !result.getFailedRows().isEmpty()) {
                        ApiResponse<AdminBulkImportResponseDto> response = new ApiResponse<>(
                                        HttpStatus.BAD_REQUEST.value(),
                                        "Validation failed. No data was saved.",
                                        result);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                ApiResponse<AdminBulkImportResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Bulk import processed successfully.",
                                result);

                return ResponseEntity.ok(response);
        }
}
