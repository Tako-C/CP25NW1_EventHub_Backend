package com.int371.eventhub.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRegistrationStatsDto {
    private Integer totalParticipants;
    private Integer totalCheckin;
    private Integer totalRegistration;
    private List<GenderStatDto> genderStats;
    private List<HourlyStatDto> hourlyStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyStatDto {
        private String hourRange;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleRatioStatDto {
        private String roleName;
        private Long total;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityStatDto {
        private String cityName;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenderStatDto {
        private String genderName;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeRangeStatDto {
        private String ageRange;
        private Long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobStatDto {
        private String jobName;
        private Long total;
    }
}
