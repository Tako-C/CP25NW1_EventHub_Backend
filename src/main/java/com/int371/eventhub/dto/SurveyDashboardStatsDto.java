package com.int371.eventhub.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDashboardStatsDto {
    private Integer visitorSubPreSurvey;
    private Integer exhibitorSubPreSurvey;
    private Integer visitorSubPostSurvey;
    private Integer exhibitorSubPostSurvey;
    private Integer totalSubPreSurvey;
    private Integer totalSubPostSurvey;

    private Double surveyCompletionRate;
    private Integer totalEmailsSent;

    private List<DashboardRegistrationStatsDto.HourlyStatDto> hourlyPostSurveyStats;
}
