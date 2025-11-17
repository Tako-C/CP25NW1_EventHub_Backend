package com.int371.eventhub.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "MEMBER_EVENTS")
public class MemberEvent {

    @EmbeddedId
    private MemberEventId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "EVENT_ID")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private MemberEventStatus status;

    @ManyToOne
    @JoinColumn(name = "EVENT_ROLE_ID", nullable = false)
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
        this.id = new MemberEventId(user.getId(), event.getId());
        this.status = MemberEventStatus.registration;
        this.eventRole = eventRole;
    }

    public MemberEvent() {
    }

    @LastModifiedDate
    @Column(name = "CHECK_IN_AT", nullable = false)
    private LocalDateTime updatedAt;
}