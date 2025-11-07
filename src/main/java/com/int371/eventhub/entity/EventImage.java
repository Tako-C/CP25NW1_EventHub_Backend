package com.int371.eventhub.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "IMAGES")
public class EventImage {
    @Id
    private Integer id;

    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "IMG_PATH_EV")
    private String imgPathEv;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "ID")
    private ImageCategory category;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Column(name = "DESCRIPTION")
    private Integer description;

}
