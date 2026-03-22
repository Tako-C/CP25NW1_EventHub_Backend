package com.int371.eventhub.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "AI_EVENT_ANALYSIS")
public class AiEventAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "CHECK_IN_RATE")
    private Double checkInRate;

    @Column(name = "SURVEY_RATE")
    private Double surveyRate;

    @Column(name = "VISITOR_SATISFACTION", columnDefinition = "CLOB")
    private String visitorSatisfaction;

    @Column(name = "EXHIBITOR_SATISFACTION", columnDefinition = "CLOB")
    private String exhibitorSatisfaction;

    @Column(name = "EXECUTIVE_SUMMARY", columnDefinition = "CLOB")
    private String executiveSummary;

    @Column(name = "STRENGTHS", columnDefinition = "CLOB")
    private String strengths;

    @Column(name = "CRITICAL_ISSUES", columnDefinition = "CLOB")
    private String criticalIssues;

    @Column(name = "STRATEGIC_PLAN", columnDefinition = "CLOB")
    private String strategicPlan;

    @Column(name = "RAW_JSON_RESULT", columnDefinition = "CLOB")
    private String rawJsonResult;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
}
