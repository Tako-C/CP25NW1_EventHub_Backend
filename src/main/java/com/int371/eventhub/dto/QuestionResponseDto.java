package com.int371.eventhub.dto;

import java.util.ArrayList;
import java.util.List;

import com.int371.eventhub.entity.QuestionType;

import lombok.Data;

@Data
public class QuestionResponseDto {
    private Integer id;
    private String question;
    private QuestionType questionType;
    private List<String> choices;

    // Helper method to set choices from individual answer strings
    public void setChoicesFromAnswers(String a1, String a2, String a3, String a4, String a5) {
        this.choices = new ArrayList<>();
        if (a1 != null && !a1.isBlank()) choices.add(a1);
        if (a2 != null && !a2.isBlank()) choices.add(a2);
        if (a3 != null && !a3.isBlank()) choices.add(a3);
        if (a4 != null && !a4.isBlank()) choices.add(a4);
        if (a5 != null && !a5.isBlank()) choices.add(a5);
    }
}