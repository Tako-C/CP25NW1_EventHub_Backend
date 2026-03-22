package com.int371.eventhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalyzeRequestDto {
    @JsonProperty("rs_id")
    private String rsId;

    private String suggestion;
}
