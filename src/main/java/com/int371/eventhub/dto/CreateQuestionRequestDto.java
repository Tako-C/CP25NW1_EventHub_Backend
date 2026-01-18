package com.int371.eventhub.dto;

import java.util.List;

import com.int371.eventhub.entity.QuestionType;

import lombok.Data;

@Data
public class CreateQuestionRequestDto {
    private String question;
    private QuestionType questionType;
    private List<String> choices; // รับเป็น array ["A", "B", "C"]
}