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

    @Column(name = "SCORE_VISITOR_5_COUNT")
    private Integer scoreVisitor5Count;

    @Column(name = "SCORE_VISITOR_4_COUNT")
    private Integer scoreVisitor4Count;

    @Column(name = "SCORE_VISITOR_3_COUNT")
    private Integer scoreVisitor3Count;

    @Column(name = "SCORE_VISITOR_2_COUNT")
    private Integer scoreVisitor2Count;

    @Column(name = "SCORE_VISITOR_1_COUNT")
    private Integer scoreVisitor1Count;

    @Column(name = "SCORE_EXHIBITOR_5_COUNT")
    private Integer scoreExhibitor5Count;

    @Column(name = "SCORE_EXHIBITOR_4_COUNT")
    private Integer scoreExhibitor4Count;

    @Column(name = "SCORE_EXHIBITOR_3_COUNT")
    private Integer scoreExhibitor3Count;

    @Column(name = "SCORE_EXHIBITOR_2_COUNT")
    private Integer scoreExhibitor2Count;

    @Column(name = "SCORE_EXHIBITOR_1_COUNT")
    private Integer scoreExhibitor1Count;

}
