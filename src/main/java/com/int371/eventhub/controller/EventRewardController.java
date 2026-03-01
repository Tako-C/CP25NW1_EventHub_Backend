package com.int371.eventhub.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.CreateEventRewardRequestDto;
import com.int371.eventhub.dto.EventRewardResponseDto;
import com.int371.eventhub.dto.RedeemRewardRequest;
import com.int371.eventhub.service.EventRewardService;
// import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/events")
public class EventRewardController {

        @Autowired
        private EventRewardService eventRewardService;

        @PostMapping(value = "/{eventId}/rewards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<EventRewardResponseDto>> createReward(
                        @PathVariable Integer eventId,
                        @ModelAttribute CreateEventRewardRequestDto request,
                        Principal principal) {

                EventRewardResponseDto createdReward = eventRewardService.createReward(eventId, request,
                                principal.getName());

                ApiResponse<EventRewardResponseDto> response = new ApiResponse<>(
                                HttpStatus.CREATED.value(),
                                "Reward created successfully",
                                createdReward);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping(value = "/{eventId}/rewards/{rewardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<EventRewardResponseDto>> updateReward(
                        @PathVariable Integer eventId,
                        @PathVariable Integer rewardId,
                        @ModelAttribute CreateEventRewardRequestDto request,
                        Principal principal) {

                EventRewardResponseDto updatedReward = eventRewardService.updateReward(eventId, rewardId, request,
                                principal.getName());

                ApiResponse<EventRewardResponseDto> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Reward updated successfully",
                                updatedReward);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{eventId}/rewards/{rewardId}")
        public ResponseEntity<ApiResponse<Void>> deleteReward(
                        @PathVariable Integer eventId,
                        @PathVariable Integer rewardId,
                        Principal principal) {

                eventRewardService.deleteReward(eventId, rewardId, principal.getName());

                ApiResponse<Void> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Reward deleted successfully",
                                null);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{eventId}/rewards/organizer")
        public ResponseEntity<ApiResponse<java.util.List<EventRewardResponseDto>>> getRewardsForOrganizer(
                        @PathVariable Integer eventId,
                        Principal principal) {

                java.util.List<EventRewardResponseDto> data = eventRewardService.getRewardsForOrganizer(eventId,
                                principal.getName());

                ApiResponse<java.util.List<EventRewardResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Organizer rewards fetched successfully",
                                data);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/rewards")
        public ResponseEntity<ApiResponse<java.util.List<EventRewardResponseDto>>> getAllRewards(
                        Principal principal) {

                java.util.List<EventRewardResponseDto> data = eventRewardService.getAllRewards();

                ApiResponse<java.util.List<EventRewardResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "All rewards fetched successfully",
                                data);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{eventId}/rewards/visitor")
        public ResponseEntity<ApiResponse<java.util.List<EventRewardResponseDto>>> getRewardsForVisitor(
                        @PathVariable Integer eventId,
                        Principal principal) {

                java.util.List<EventRewardResponseDto> data = eventRewardService.getRewardsForVisitor(eventId,
                                principal.getName());

                ApiResponse<java.util.List<EventRewardResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Visitor rewards fetched successfully",
                                data);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/rewards/{userId}")
        public ResponseEntity<ApiResponse<List<EventRewardResponseDto>>> getRewardsByUserId(
                        @PathVariable Integer userId,
                        Principal principal) {

                java.util.List<EventRewardResponseDto> data = eventRewardService.getRewardsByUserId(userId);

                ApiResponse<java.util.List<EventRewardResponseDto>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "User rewards fetched successfully",
                                data);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{eventId}/rewards/redeem")
        public ResponseEntity<ApiResponse<String>> redeemReward(
                        @PathVariable Integer eventId,
                        @org.springframework.web.bind.annotation.RequestBody RedeemRewardRequest request) {

                String message = eventRewardService.redeemReward(request);

                ApiResponse<String> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                message,
                                null);
                return ResponseEntity.ok(response);
        }
}
