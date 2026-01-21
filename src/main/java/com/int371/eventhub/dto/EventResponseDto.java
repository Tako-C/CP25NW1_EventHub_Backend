package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import com.int371.eventhub.entity.EventStatus;
import com.int371.eventhub.entity.EventType;

import lombok.Data;

@Data
public class EventResponseDto {
    private Integer id;
    private String eventName;
    private String eventDesc;
    private EventType eventTypeId;
    private String location;
    private String hostOrganisation;
    private Integer createdBy;
    private String contactLine;
    private String contactEmail;
    private String contactFacebook;
    private String contactPhone;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EventStatus status;
    private EventImageResponseDto images;
}
