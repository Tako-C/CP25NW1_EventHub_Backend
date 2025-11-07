package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "EVENT_TYPES")
public class EventType {
    @Id
    private Integer id;

    @Column(name = "NAME")
    private String eventTypeName;

    @Column(name = "DESCRIPTION")
    private String eventTypeDescription;
}
