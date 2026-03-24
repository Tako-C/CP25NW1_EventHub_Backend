package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.DashboardRegistrationStatsDto;
import com.int371.eventhub.dto.DashboardRegistrationStatsDto.GenderStatDto;
import com.int371.eventhub.dto.DashboardRegistrationStatsDto.HourlyStatDto;
import com.int371.eventhub.dto.SatisfactionKpiDto;
import com.int371.eventhub.dto.SurveyDashboardStatsDto;
import com.int371.eventhub.dto.SurveyStatusListDto;
import com.int371.eventhub.dto.TextResponseDto;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.ResponseAnswerRepository;

@Service
public class EventDashboardService {

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired
    private ResponseAnswerRepository responseAnswerRepository;

    public DashboardRegistrationStatsDto getEventRegistrationStats(Integer eventId) {
        return getStatsForStatuses(eventId, java.util.Arrays.asList("REGISTRATION", "CHECK_IN"), false);
    }

    public DashboardRegistrationStatsDto getEventCheckInStats(Integer eventId) {
        return getStatsForStatuses(eventId, java.util.Collections.singletonList("CHECK_IN"), true);
    }

    public List<SatisfactionKpiDto> getSatisfactionKpi(Integer eventId, MemberEventRole role) {
        java.util.List<SurveyType> surveyTypes = role == MemberEventRole.VISITOR
                ? java.util.Arrays.asList(SurveyType.PRE_VISITOR, SurveyType.POST_VISITOR)
                : java.util.Arrays.asList(SurveyType.PRE_EXHIBITOR, SurveyType.POST_EXHIBITOR);

        List<Object[]> rawCounts = responseAnswerRepository.countSatisfactionByEventAndSurveyTypes(eventId, surveyTypes);
        long totalResponses = rawCounts.stream().mapToLong(obj -> ((Number) obj[1]).longValue()).sum();

        return rawCounts.stream()
                .map(obj -> {
                    String answer = (String) obj[0];
                    long count = ((Number) obj[1]).longValue();
                    double percentage = totalResponses > 0 ? (double) count / totalResponses * 100 : 0;
                    percentage = Math.round(percentage * 100.0) / 100.0;
                    return new SatisfactionKpiDto(answer, count, percentage);
                })
                .collect(Collectors.toList());
    }

    public List<TextResponseDto> getTextResponses(Integer eventId) {
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
        Long preCount = memberEventRepository.countByEventIdAndEventRoleAndDonePreSurvey(eventId, role, 1);
        Long postCount = memberEventRepository.countByEventIdAndEventRoleAndDonePostSurvey(eventId, role, 1);
        Long bothCount = memberEventRepository.countByEventIdAndEventRoleAndDonePreSurveyAndDonePostSurvey(eventId, role,
                1, 1);
        
        Long allPreCount = memberEventRepository.countParticipantPreSurvey(eventId, 1);
        Long allPostCount = memberEventRepository.countParticipantPostSurvey(eventId, 1);
        Long allBothCount = memberEventRepository.countParticipantBothSurveys(eventId, 1, 1);

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

        return new SurveyDashboardStatsDto(
                preCount != null ? preCount : 0L,
                postCount != null ? postCount : 0L,
                bothCount != null ? bothCount : 0L,
                allPreCount != null ? allPreCount : 0L,
                allPostCount != null ? allPostCount : 0L,
                allBothCount != null ? allBothCount : 0L,
                hourlyPostSurveyStats);
    }

    public List<SurveyStatusListDto> getSurveyStatusList(Integer eventId, MemberEventRole role) {
        return memberEventRepository.findSurveyStatusByEventIdAndRole(eventId, role);
    }

    private DashboardRegistrationStatsDto getStatsForStatuses(Integer eventId, List<String> statuses,
            boolean isCheckIn) {
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
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> jobData = memberEventRepository.countJobsByEventIdAndStatuses(eventId, statuses);
        return jobData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.JobStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.GenderStatDto> getEventGenderStats(Integer eventId) {
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> genderData = memberEventRepository.countGendersByEventIdAndStatuses(eventId, statuses);
        return genderData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.GenderStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.AgeRangeStatDto> getEventAgeStats(Integer eventId) {
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> ageRangeData = memberEventRepository.countAgeRangesByEventIdAndStatuses(eventId, statuses);
        return ageRangeData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.AgeRangeStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.CityStatDto> getEventCityStats(Integer eventId) {
        List<String> statuses = java.util.Arrays.asList("REGISTRATION", "CHECK_IN");
        List<Object[]> cityData = memberEventRepository.countCitiesByEventIdAndStatuses(eventId, statuses);
        return cityData.stream()
                .map(obj -> new DashboardRegistrationStatsDto.CityStatDto((String) obj[0],
                        ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<DashboardRegistrationStatsDto.RoleRatioStatDto> getEventRoleRatioStats(Integer eventId) {
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
