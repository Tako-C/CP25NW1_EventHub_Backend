package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAnswerSearchDto {
    private Long id;
    private Integer questionId;
    private String questionText;
    private String answer;
    private String keyword;
    private String sentiment;
    private String questionType;
    private Integer eventId;
}
