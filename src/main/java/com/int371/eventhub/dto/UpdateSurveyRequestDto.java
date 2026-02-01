package com.int371.eventhub.dto;

import java.util.List;

import com.int371.eventhub.entity.SurveyType;

import lombok.Data;

@Data
public class UpdateSurveyRequestDto {
    private String name;
    private String description;
    private Integer points;
    private SurveyType surveyType;
    private List<UpdateQuestionDto> questions;
}
