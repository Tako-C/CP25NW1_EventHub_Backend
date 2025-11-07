package com.int371.eventhub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class EventImageResponse {
    
    @JsonProperty("imgDetail")
    private String imageDetail;

    @JsonProperty("imgCard")
    private String imageCard;

    @JsonProperty("imgSlideShow")
    private String imageSlideShow;

    @JsonProperty("imgMap")
    private String imageMap;
}