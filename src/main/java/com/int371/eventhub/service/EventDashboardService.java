package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.DashboardRegistrationStatsDto;
import com.int371.eventhub.dto.DashboardRegistrationStatsDto.GenderStatDto;
import com.int371.eventhub.repository.MemberEventRepository;

@Service
public class EventDashboardService {

    @Autowired
    private MemberEventRepository memberEventRepository;

    public DashboardRegistrationStatsDto getEventRegistrationStats(Integer eventId) {
        return getStatsForStatuses(eventId, java.util.Arrays.asList("REGISTRATION", "CHECK_IN"));
    }

    public DashboardRegistrationStatsDto getEventCheckInStats(Integer eventId) {
        return getStatsForStatuses(eventId, java.util.Collections.singletonList("CHECK_IN"));
    }

    private DashboardRegistrationStatsDto getStatsForStatuses(Integer eventId, List<String> statuses) {
        Integer totalParticipantsCount = memberEventRepository.countByEventId(eventId);
        Integer totalCheckinCount = memberEventRepository.countByEventIdAndStatus(eventId,
                com.int371.eventhub.entity.MemberEventStatus.CHECK_IN);
        Integer totalRegistrationCount = memberEventRepository.countByEventIdAndStatus(eventId,
                com.int371.eventhub.entity.MemberEventStatus.REGISTRATION);

        List<Object[]> genderData = memberEventRepository.countGendersByEventIdAndStatuses(eventId, statuses);
        List<GenderStatDto> genderStats = genderData.stream()
                .map(obj -> new GenderStatDto((String) obj[0], ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());

        return new DashboardRegistrationStatsDto(
                totalParticipantsCount != null ? totalParticipantsCount : 0,
                totalCheckinCount != null ? totalCheckinCount : 0,
                totalRegistrationCount != null ? totalRegistrationCount : 0,
                genderStats);
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
