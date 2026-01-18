package com.int371.eventhub.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "EVENTS")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID",insertable = false, updatable = false)
    private Integer id;

    @Column(name = "NAME")
    private String eventName;

    @Lob
    @Column(name = "DESCRIPTION")
    private String eventDesc;

    @ManyToOne
    @JoinColumn(name = "TYPE_ID")
    private EventType eventTypeId;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "HOST_ORGANISATION")
    private String hostOrganisation;

    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "CONTACT_LINE")
    private String contactLine;

    @Column(name = "CONTACT_EMAIL")
    private String contactEmail;

    @Column(name = "CONTACT_FACEBOOK")
    private String contactFacebook;

    @Column(name = "CONTACT_PHONE")
    private String contactPhone;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false )
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    // @OneToMany(fetch = FetchType.LAZY)
    // @JoinColumn(name = "EVENT_ID", referencedColumnName = "ID")
    // private List<EventImage> images;

    // ใน Event.java
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> images = new ArrayList<>();
}
