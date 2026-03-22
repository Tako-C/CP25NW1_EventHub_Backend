package com.int371.eventhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentCategoryDto {
    private String category;
    private Integer count;
    
    @JsonProperty("event_role")
    private String eventRole;
    
    @JsonProperty("example_text")
    private String exampleText;
    
    private String sentiment;
}
