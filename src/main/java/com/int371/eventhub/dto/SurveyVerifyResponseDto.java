package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyVerifyResponseDto {
    private Integer eventId;
    private String eventName;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String eventRole; 
    private String accessToken;
}