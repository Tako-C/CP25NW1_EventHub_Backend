package com.int371.eventhub.dto;

import java.util.List;

import lombok.Data;

@Data
public class SurveySubmissionRequestDto {
    private List<SurveyAnswerRequestDto> answers;
}