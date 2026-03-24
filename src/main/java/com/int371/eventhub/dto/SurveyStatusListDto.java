package com.int371.eventhub.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyStatusListDto {
    private String firstName;
    private String lastName;
    private Boolean preSurveyDone;
    private Boolean postSurveyDone;
    private LocalDateTime postSurveySubmittedAt;
}
