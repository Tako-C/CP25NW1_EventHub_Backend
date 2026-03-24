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
import com.int371.eventhub.dto.SatisfactionKpiDto;
import com.int371.eventhub.dto.SurveyDashboardStatsDto;
import com.int371.eventhub.dto.SurveyStatusListDto;
import com.int371.eventhub.dto.TextResponseDto;
import com.int371.eventhub.entity.MemberEventRole;
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

    @GetMapping("/{eventId}/surveys/visitor/stats")
    public ResponseEntity<ApiResponse<SurveyDashboardStatsDto>> getVisitorSurveyStats(
            @PathVariable Integer eventId) {

        SurveyDashboardStatsDto stats = eventDashboardService.getSurveyStats(eventId, MemberEventRole.VISITOR);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched visitor survey stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/surveys/visitor/status")
    public ResponseEntity<ApiResponse<List<SurveyStatusListDto>>> getVisitorSurveyStatusList(
            @PathVariable Integer eventId) {

        List<SurveyStatusListDto> statusList = eventDashboardService.getSurveyStatusList(eventId,
                MemberEventRole.VISITOR);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched visitor survey status list successfully.",
                statusList));
    }

    @GetMapping("/{eventId}/surveys/exhibitor/stats")
    public ResponseEntity<ApiResponse<SurveyDashboardStatsDto>> getExhibitorSurveyStats(
            @PathVariable Integer eventId) {

        SurveyDashboardStatsDto stats = eventDashboardService.getSurveyStats(eventId, MemberEventRole.EXHIBITOR);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched exhibitor survey stats successfully.",
                stats));
    }

    @GetMapping("/{eventId}/surveys/exhibitor/status")
    public ResponseEntity<ApiResponse<List<SurveyStatusListDto>>> getExhibitorSurveyStatusList(
            @PathVariable Integer eventId) {

        List<SurveyStatusListDto> statusList = eventDashboardService.getSurveyStatusList(eventId,
                MemberEventRole.EXHIBITOR);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched exhibitor survey status list successfully.",
                statusList));
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
    @GetMapping("/{eventId}/surveys/visitor/satisfaction")
    public ResponseEntity<ApiResponse<List<SatisfactionKpiDto>>> getVisitorSatisfactionKpi(
            @PathVariable Integer eventId) {

        List<SatisfactionKpiDto> kpi = eventDashboardService.getSatisfactionKpi(eventId, MemberEventRole.VISITOR);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched visitor satisfaction KPI successfully.",
                kpi));
    }

    @GetMapping("/{eventId}/surveys/exhibitor/satisfaction")
    public ResponseEntity<ApiResponse<List<SatisfactionKpiDto>>> getExhibitorSatisfactionKpi(
            @PathVariable Integer eventId) {

        List<SatisfactionKpiDto> kpi = eventDashboardService.getSatisfactionKpi(eventId, MemberEventRole.EXHIBITOR);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched exhibitor satisfaction KPI successfully.",
                kpi));
    }

    @GetMapping("/{eventId}/surveys/text-responses")
    public ResponseEntity<ApiResponse<List<TextResponseDto>>> getTextResponses(
            @PathVariable Integer eventId) {

        List<TextResponseDto> responses = eventDashboardService.getTextResponses(eventId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Fetched text responses successfully.",
                responses));
    }
}
