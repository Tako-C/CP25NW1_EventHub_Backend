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
@Table(name = "VISITORS_EVENTS")
public class VisitorEvent {

    @EmbeddedId
    private VisitorEventId id;

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
    private VisitorEventStatus status;

    @Column(name = "IMG_PATH_QR")
    private String imgPathQr;

    @CreatedDate
    @Column(name = "REGISTRATION_AT", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public VisitorEvent(User user, Event event) {
        this.user = user;
        this.event = event;
        this.id = new VisitorEventId(user.getId(), event.getId());
        this.status = VisitorEventStatus.registration;
    }

    public VisitorEvent() {
    }

    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
}