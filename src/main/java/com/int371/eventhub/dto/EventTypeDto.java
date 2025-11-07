package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class EventTypeDto {
    private Integer id;
    private String eventTypeName;
    private String eventTypeDescription;
}
