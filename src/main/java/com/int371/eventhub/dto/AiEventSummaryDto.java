package com.int371.eventhub.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AiEventSummaryDto {
    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("location")
    private String location;

    @JsonProperty("event_detail")
    private String eventDetail;

    @JsonProperty("total_registered")
    private Integer totalRegistered;

    @JsonProperty("total_checked_in")
    private Integer totalCheckedIn;

    @JsonProperty("total_feedback")
    private Integer totalFeedback;

    private Map<String, Integer> occupations;

    @JsonProperty("visitor_score")
    private Double visitorScore;

    @JsonProperty("exhibitor_score")
    private Double exhibitorScore;

    @JsonProperty("returning_visitor_rate")
    private Double returningVisitorRate;

    @JsonProperty("top_issues")
    private Object topIssues;

    @JsonProperty("top_good")
    private Object topGood;
}
