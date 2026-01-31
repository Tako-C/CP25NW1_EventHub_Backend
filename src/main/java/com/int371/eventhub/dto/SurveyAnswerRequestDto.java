package com.int371.eventhub.dto;

import java.util.List;

import lombok.Data;

@Data
public class SurveyAnswerRequestDto {
    private Integer questionId;
    private List<String> answers; 
}