package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyGroupResponseDto {
    private List<SurveyResponseDto> visitor;
    private List<SurveyResponseDto> exhibitor;
}