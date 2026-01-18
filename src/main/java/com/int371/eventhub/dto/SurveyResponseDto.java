package com.int371.eventhub.dto;

import java.util.List;

import com.int371.eventhub.entity.SurveyStatus;
import com.int371.eventhub.entity.SurveyType;

import lombok.Data;

@Data
public class SurveyResponseDto {
    private Integer id;
    private String name;
    private String description;
    private Integer points;
    private SurveyStatus status;
    private SurveyType type;

    private List<QuestionResponseDto> questions;
}