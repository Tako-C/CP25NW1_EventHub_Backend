package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextResponseDto {
    private String responderRole;
    private String answer;
    private String surveysType;
    private String keyword;
    private String sentiment;
}
