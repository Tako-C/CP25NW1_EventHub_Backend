package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventResponse {
    private Integer id;
    private String eventName;
    private String eventDesc;
    private Integer eventTypeId;
    private String location;
    private String hostOrganisation;
    private Integer createdBy;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
