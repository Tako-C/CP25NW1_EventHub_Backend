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
@Table(name = "V_SATISFACTION_KPI")
public class SatisfactionKpi {

    @Id
    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "AVG_SATISFACTION_SCORE")
    private Double avgSatisfactionScore;

    @Column(name = "VISITOR_AVG_SCORE")
    private Double visitorAvgScore;

    @Column(name = "EXHIBITOR_AVG_SCORE")
    private Double exhibitorAvgScore;

    @Column(name = "SCORE_5_COUNT")
    private Integer score5Count;

    @Column(name = "SCORE_4_COUNT")
    private Integer score4Count;

    @Column(name = "LOW_SCORE_COUNT")
    private Integer lowScoreCount;

}
