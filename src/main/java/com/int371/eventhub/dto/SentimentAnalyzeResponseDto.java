package com.int371.eventhub.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentimentAnalyzeResponseDto {
    @JsonProperty("rs_id")
    private String rsId;
    
    private String sentiment;
    private String keyword;
}
