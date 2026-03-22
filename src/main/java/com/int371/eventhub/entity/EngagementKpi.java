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
@Table(name = "V_ENGAGEMENT_KPI")
public class EngagementKpi {

    @Id
    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "REG_STAFF")
    private Integer regStaff;

    @Column(name = "REG_EXHIBITOR")
    private Integer regExhibitor;

    @Column(name = "REG_VISITOR")
    private Integer regVisitor;

    @Column(name = "REG_MALE")
    private Integer regMale;

    @Column(name = "REG_FREMALE")
    private Integer regFemale;

    @Column(name = "REG_OTHER")
    private Integer regOther;

    @Column(name = "TOTAL_REGISTERED")
    private Integer totalRegistered;

    @Column(name = "TOTAL_CHECKED_IN")
    private Integer totalCheckedIn;

    @Column(name = "CHECKIN_STAFF")
    private Integer checkinStaff;

    @Column(name = "CHECKIN_EXHIBITOR")
    private Integer checkinExhibitor;

    @Column(name = "CHECKIN_VISITOR")
    private Integer checkinVisitor;

    @Column(name = "CHECKIN_MALE")
    private Integer checkinMale;

    @Column(name = "CHECKIN_FEMALE")
    private Integer checkinFemale;

    @Column(name = "CHECKIN_OTHER")
    private Integer checkinOther;

}
