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

    @JsonProperty("total_pre_feedback")
    private Integer totalPreFeedback;

    @JsonProperty("total_pos_feedback")
    private Integer totalPosFeedback;

    @JsonProperty("total_submit_pre_v_feedback")
    private Integer totalSubmitPreVFeedback;

    @JsonProperty("total_submit_pos_v_feedback")
    private Integer totalSubmitPosVFeedback;

    @JsonProperty("total_submit_pre_e_feedback")
    private Integer totalSubmitPreEFeedback;

    @JsonProperty("total_submit_pos_e_feedback")
    private Integer totalSubmitPosEFeedback;

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
