package com.int371.eventhub.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "EVENTS")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "EVENT_NAME")
    private String eventName;

    @Lob
    @Column(name = "EVENT_DESC")
    private String eventDesc;

    @Column(name = "EVENT_TYPE_ID")
    private Integer eventTypeId;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "HOST_ORGANISATION")
    private String hostOrganisation;

    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
