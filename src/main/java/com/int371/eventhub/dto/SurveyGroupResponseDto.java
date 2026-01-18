package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyGroupResponseDto {
    private SurveyResponseDto visitor;
    private SurveyResponseDto exhibitor;
}