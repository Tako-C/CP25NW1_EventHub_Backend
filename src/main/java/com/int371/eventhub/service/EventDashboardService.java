package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.DashboardRegistrationStatsDto;
import com.int371.eventhub.dto.DashboardRegistrationStatsDto.GenderStatDto;
import com.int371.eventhub.dto.DashboardRegistrationStatsDto.HourlyStatDto;
import com.int371.eventhub.dto.SatisfactionKpiDto;
import com.int371.eventhub.dto.SatisfactionResponseDto;
import com.int371.eventhub.dto.SurveyDashboardStatsDto;
import com.int371.eventhub.dto.SurveyStatusListDto;
import com.int371.eventhub.dto.TextResponseDto;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.OperationalKpi;
import com.int371.eventhub.entity.SatisfactionKpi;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.OperationalKpiRepository;
import com.int371.eventhub.repository.ResponseAnswerRepository;
import com.int371.eventhub.repository.SatisfactionKpiRepository;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.exception.ResourceNotFoundException;

@Service
public class EventDashboardService {

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired
    private ResponseAnswerRepository responseAnswerRepository;

    @Autowired
    private SatisfactionKpiRepository satisfactionKpiRepository;

    @Autowired
    private OperationalKpiRepository operationalKpiRepository;

    @Autowired
    private EventRepository eventRepository;

    private void validateEventExists(Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
    }

    public DashboardRegistrationStatsDto getEventRegistrationStats(Integer eventId) {
        return getStatsForStatuses(eventId, java.util.Arrays.asList("REGISTRATION", "CHECK_IN"), false);
    }

    public DashboardRegistrationStatsDto getEventCheckInStats(Integer eventId) {
        return getStatsForStatuses(eventId, java.util.Collections.singletonList("CHECK_IN"), true);
    }

    public SatisfactionResponseDto getSatisfactionKpi(Integer eventId, MemberEventRole role) {
        validateEventExists(eventId);
        SatisfactionKpi kpiEntity = satisfactionKpiRepository.findByEventId(eventId).orElse(null);
        if (kpiEntity == null) {
            return new SatisfactionResponseDto(0.0, 0.0, java.util.Collections.emptyList());
        }

        Double avgScore = role == MemberEventRole.VISITOR ? kpiEntity.getVisitorAvgScore() : kpiEntity.getExhibitorAvgScore();

        List<SatisfactionKpiDto> kpiList = new java.util.ArrayList<>();
        if (role == MemberEventRole.VISITOR) {
            kpiList.add(new SatisfactionKpiDto("5", (long) (kpiEntity.getScoreVisitor5Count() != null ? kpiEntity.getScoreVisitor5Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("4", (long) (kpiEntity.getScoreVisitor4Count() != null ? kpiEntity.getScoreVisitor4Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("3", (long) (kpiEntity.getScoreVisitor3Count() != null ? kpiEntity.getScoreVisitor3Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("2", (long) (kpiEntity.getScoreVisitor2Count() != null ? kpiEntity.getScoreVisitor2Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("1", (long) (kpiEntity.getScoreVisitor1Count() != null ? kpiEntity.getScoreVisitor1Count() : 0), 0.0));
        } else {
            kpiList.add(new SatisfactionKpiDto("5", (long) (kpiEntity.getScoreExhibitor5Count() != null ? kpiEntity.getScoreExhibitor5Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("4", (long) (kpiEntity.getScoreExhibitor4Count() != null ? kpiEntity.getScoreExhibitor4Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("3", (long) (kpiEntity.getScoreExhibitor3Count() != null ? kpiEntity.getScoreExhibitor3Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("2", (long) (kpiEntity.getScoreExhibitor2Count() != null ? kpiEntity.getScoreExhibitor2Count() : 0), 0.0));
            kpiList.add(new SatisfactionKpiDto("1", (long) (kpiEntity.getScoreExhibitor1Count() != null ? kpiEntity.getScoreExhibitor1Count() : 0), 0.0));
        }

        long total = kpiList.stream().mapToLong(SatisfactionKpiDto::getCount).sum();
        for (SatisfactionKpiDto dto : kpiList) {
            double percentage = total > 0 ? (double) dto.getCount() / total * 100 : 0;
            dto.setPercentage(Math.round(percentage * 100.0) / 100.0);
        }

        return new SatisfactionResponseDto(avgScore != null ? avgScore : 0.0,
                kpiEntity.getAvgSatisfactionScore() != null ? kpiEntity.getAvgSatisfactionScore() : 0.0,
                kpiList);
    }

    public List<TextResponseDto> getTextResponses(Integer eventId) {
        validateEventExists(eventId);
        List<Object[]> rawData = responseAnswerRepository.findFullTextResponsesByEventId(eventId);
        return rawData.stream()
                .map(obj -> new TextResponseDto(
                        ((MemberEventRole) obj[0]).name(),
                        (String) obj[1],
                        ((SurveyType) obj[2]).name(),
                        (String) obj[3],
                        (String) obj[4]))
                .collect(Collectors.toList());
    }

    public SurveyDashboardStatsDto getSurveyStats(Integer eventId, MemberEventRole role) {
        validateEventExists(eventId);
        OperationalKpi opKpi = operationalKpiRepository.findByEventId(eventId).orElse(null);

        List<Object[]> hourlyData = responseAnswerRepository.countHourlySubmissionsByEventIdAndRole(eventId,
                role.name());

        java.util.Map<String, Long> hourMap = hourlyData.stream()
                .collect(java.util.stream.Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> ((Number) obj[1]).longValue(),
                        (v1, v2) -> v1));

        List<HourlyStatDto> hourlyPostSurveyStats = new java.util.ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String hourStr = String.format("%02d", i);
            String range = String.format("%02d.00 - %02d.00", i, (i + 1) % 24);
            Long count = hourMap.getOrDefault(hourStr, 0L);
            hourlyPostSurveyStats.add(new HourlyStatDto(range, count));
        }

        if (opKpi == null) {
            return new SurveyDashboardStatsDto(0, 0, 0, 0, 0, 0, 0.0, 0, hourlyPostSurveyStats);
        }

        return new SurveyDashboardStatsDto(
                opKpi.getVisitorSubPreSurvey() != null ? opKpi.getVisitorSubPreSurvey() : 0,
                opKpi.getExhibitorSubPreSurvey() != null ? opKpi.getExhibitorSubPreSurvey() : 0,
                opKpi.getVisitorSubPostSurvey() != null ? opKpi.getVisitorSubPostSurvey() : 0,
                opKpi.getExhibitorSubPostSurvey() != null ? opKpi.getExhibitorSubPostSurvey() : 0,
                opKpi.getTotalSubPreSurvey() != null ? opKpi.getTotalSubPreSurvey() : 0,
                opKpi.getTotalSubPostSurvey() != null ? opKpi.getTotalSubPostSurvey() : 0,
                opKpi.getSurveyCompletionRate() != null ? opKpi.getSurveyCompletionRate() : 0.0,
                opKpi.getTotalEmailsSent() != null ? opKpi.getTotalEmailsSent() : 0,
                hourlyPostSurveyStats);
    }

    public List<SurveyStatusListDto> getSurveyStatusList(Integer eventId, MemberEventRole role) {
        validateEventExists(eventId);
        return memberEventRepository.findSurveyStatusByEventIdAndRole(eventId, role);
    }

    private DashboardRegistrationStatsDto getStatsForStatuses(Integer eventId, List<String> statuses,
            boolean isCheckIn) {
        validateEventExists(eventId);
        Integer totalParticipantsCount = memberEventRepository.countByEventId(eventId);
        Integer totalCheckinCount = memberEventRepository.countByEventIdAndStatus(eventId,
                com.int371.eventhub.entity.MemberEventStatus.CHECK_IN);
        Integer totalRegistrationCount = memberEventRepository.countByEventIdAndStatus(eventId,
                com.int371.eventhub.entity.MemberEventStatus.REGISTRATION);

        List<Object[]> genderData = memberEventRepository.countGendersByEventIdAndStatuses(eventId, statuses);
        List<GenderStatDto> genderStats = genderData.stream()
                .map(obj -> new GenderStatDto((String) obj[0], ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());

        List<HourlyStatDto> hourlyStats = getHourlyStatsHelper(eventId, statuses, isCheckIn);

        return new DashboardRegistrationStatsDto(
                totalParticipantsCount != null ? totalParticipantsCount : 0,
                totalCheckinCount != null ? totalCheckinCount : 0,
                totalRegistrationCount != null ? totalRegistrationCount : 0,
                genderStats,
                hourlyStats);
    }

    private List<HourlyStatDto> getHourlyStatsHelper(Integer eventId, List<String> statuses, boolean isCheckIn) {
        List<Object[]> hourlyData;
        if (isCheckIn) {
            hourlyData = memberEventRepository.countHourlyCheckInsByEventIdAndStatuses(eventId, statuses);
        } else {
            hourlyData = memberEventRepository.countHourlyRegistrationsByEventIdAndStatuses(eventId, statuses);
        }

        java.util.Map<String, Long> hourMap = hourlyData.stream()
                .collect(java.util.stream.Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> ((Number) obj[1]).longValue(),
                        (v1, v2) -> v1));

        List<HourlyStatDto> hourlyStats = new java.util.ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String hourStr = String.format("%02d", i);
            String range = String.format("%02d.00 - %02d.00", i, (i + 1) % 24);
            Long count = hourMap.getOrDefault(hourStr, 0L);
            hourlyStats.add(new HourlyStatDto(range, count));
        }
        return hourlyStats;
    }

    public List<DashboardRegistrationStatsDto.JobStatDto> getEventJobStats(Integer eventId) {
        validateEventExists(eventId);
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> jobData = memberEventRepository.countJobsByEventIdAndStatuses(eventId, statuses);
        return jobData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.JobStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.GenderStatDto> getEventGenderStats(Integer eventId) {
        validateEventExists(eventId);
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> genderData = memberEventRepository.countGendersByEventIdAndStatuses(eventId, statuses);
        return genderData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.GenderStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.AgeRangeStatDto> getEventAgeStats(Integer eventId) {
        validateEventExists(eventId);
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> ageRangeData = memberEventRepository.countAgeRangesByEventIdAndStatuses(eventId, statuses);
        return ageRangeData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.AgeRangeStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.CityStatDto> getEventCityStats(Integer eventId) {
        validateEventExists(eventId);
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> cityData = memberEventRepository.countCitiesByEventIdAndStatuses(eventId, statuses);
        return cityData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.CityStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.RoleRatioStatDto> getEventRoleRatioStats(Integer eventId) {
        validateEventExists(eventId);
        return getRoleRatioStatsHelper(eventId, java.util.Arrays.asList("REGISTRATION", "CHECK_IN"));
    }

    private List<DashboardRegistrationStatsDto.RoleRatioStatDto> getRoleRatioStatsHelper(Integer eventId,
            List<String> statuses) {
        List<Object[]> roleData = memberEventRepository.countRolesByEventIdAndStatuses(eventId, statuses);
        long totalRoles = roleData.stream().mapToLong(obj -> ((Number) obj[1]).longValue()).sum();

        return roleData.stream()
                .map(obj -> {
                    String roleName = (String) obj[0];
                    long count = ((Number) obj[1]).longValue();
                    double percentage = totalRoles > 0 ? ((double) count / totalRoles) * 100 : 0.0;
                    // Round to 2 decimal places
                    percentage = Math.round(percentage * 100.0) / 100.0;
                    return new DashboardRegistrationStatsDto.RoleRatioStatDto(roleName, count, percentage);
                })
                .collect(Collectors.toList());
    }
}
