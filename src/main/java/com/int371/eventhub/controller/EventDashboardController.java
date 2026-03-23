package com.int371.eventhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.DashboardRegistrationStatsDto;
import com.int371.eventhub.service.EventDashboardService;

@RestController
@RequestMapping("/dashboard/events")
public class EventDashboardController {

    @Autowired
    private EventDashboardService eventDashboardService;

    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<ApiResponse<DashboardRegistrationStatsDto>> getRegistrationStats(
            @PathVariable Integer eventId) {

        DashboardRegistrationStatsDto stats = eventDashboardService.getEventRegistrationStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched registration stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/check-ins")
    public ResponseEntity<ApiResponse<DashboardRegistrationStatsDto>> getCheckInStats(
            @PathVariable Integer eventId) {

        DashboardRegistrationStatsDto stats = eventDashboardService.getEventCheckInStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched check-in stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/roles")
    public ResponseEntity<ApiResponse<List<DashboardRegistrationStatsDto.RoleRatioStatDto>>> getRoleStats(
            @PathVariable Integer eventId) {

        List<DashboardRegistrationStatsDto.RoleRatioStatDto> stats = eventDashboardService
                .getEventRoleRatioStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched role stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/genders")
    public ResponseEntity<ApiResponse<java.util.List<DashboardRegistrationStatsDto.GenderStatDto>>> getGenderStats(
            @PathVariable Integer eventId) {

        java.util.List<DashboardRegistrationStatsDto.GenderStatDto> stats = eventDashboardService
                .getEventGenderStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched gender stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/ages")
    public ResponseEntity<ApiResponse<java.util.List<DashboardRegistrationStatsDto.AgeRangeStatDto>>> getAgeStats(
            @PathVariable Integer eventId) {

        java.util.List<DashboardRegistrationStatsDto.AgeRangeStatDto> stats = eventDashboardService
                .getEventAgeStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched age stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/cities")
    public ResponseEntity<ApiResponse<java.util.List<DashboardRegistrationStatsDto.CityStatDto>>> getCityStats(
            @PathVariable Integer eventId) {

        java.util.List<DashboardRegistrationStatsDto.CityStatDto> stats = eventDashboardService
                .getEventCityStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched city stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/jobs")
    public ResponseEntity<ApiResponse<java.util.List<DashboardRegistrationStatsDto.JobStatDto>>> getJobStats(
            @PathVariable Integer eventId) {

        java.util.List<DashboardRegistrationStatsDto.JobStatDto> stats = eventDashboardService
                .getEventJobStats(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched job stats successfully.",
                stats));
    }
}
