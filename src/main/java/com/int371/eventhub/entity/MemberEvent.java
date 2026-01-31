package com.int371.eventhub.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "USER_EVENTS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"USER_ID", "EVENT_ID"})
})
public class MemberEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    // @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    // @MapsId("eventId")
    @JoinColumn(name = "EVENT_ID")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private MemberEventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_ROLE", nullable = false)
    private MemberEventRole eventRole;

    @Column(name = "PRE_SURVEY_POINT")
    private Integer preSurveyPoint;

    @Column(name = "POST_SURVEY_POINT")
    private Integer postSurveyPoint;

    @Column(name = "IMG_PATH_QR")
    private String imgPathQr;

    @CreatedDate
    @Column(name = "REGISTRATION_AT", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public MemberEvent(User user, Event event, MemberEventRole eventRole) {
        this.user = user;
        this.event = event;
        this.status = MemberEventStatus.REGISTRATION;
        this.eventRole = eventRole;
    }

    public MemberEvent() {
    }

    // @LastModifiedDate
    @Column(name = "CHECK_IN_AT", nullable = false)
    private LocalDateTime updatedAt;
}