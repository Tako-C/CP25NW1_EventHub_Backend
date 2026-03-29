package com.int371.eventhub.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryDto {

    private String surveyType;
    private Integer questionId;
    private String question;
    private String questionType;
    private List<AnswerStatDto> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerStatDto {
        private String answer;
        private Long count;
    }
}
