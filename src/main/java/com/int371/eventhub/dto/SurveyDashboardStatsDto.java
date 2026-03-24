package com.int371.eventhub.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDashboardStatsDto {
    private Long totalAllPreSurvey;
    private Long totalAllPostSurvey;
    private Long totalAllBothSurveys;

    private Long totalPreSurvey;
    private Long totalPostSurvey;
    private Long totalBothSurveys;

    private List<DashboardRegistrationStatsDto.HourlyStatDto> hourlyPostSurveyStats;
}
