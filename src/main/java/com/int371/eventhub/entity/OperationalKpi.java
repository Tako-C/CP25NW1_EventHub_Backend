package com.int371.eventhub.entity;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Immutable
@Table(name = "V_OPERATIONAL_KPI")
public class OperationalKpi {

    @Id
    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "TOTAL_PRE_SURVEY")
    private Integer totalPreSurvey;

    @Column(name = "TOTAL_POST_SURVEY")
    private Integer totalPostSurvey;

    @Column(name = "VISITOR_SUB_PRE_SURVEY")
    private Integer visitorSubPreSurvey;

    @Column(name = "EXHIBITOR_SUB_PRE_SURVEY")
    private Integer exhibitorSubPreSurvey;

    @Column(name = "VISITOR_SUB_POST_SURVEY")
    private Integer visitorSubPostSurvey;

    @Column(name = "EXHIBITOR_SUB_POST_SURVEY")
    private Integer exhibitorSubPostSurvey;

    @Column(name = "TOTAL_SUB_PRE_SURVEY")
    private Integer totalSubPreSurvey;

    @Column(name = "TOTAL_SUB_POST_SURVEY")
    private Integer totalSubPostSurvey;

    @Column(name = "SURVEY_COMPLETION_RATE")
    private Double surveyCompletionRate;

    @Column(name = "TOTAL_EMAILS_SENT")
    private Integer totalEmailsSent;

}
