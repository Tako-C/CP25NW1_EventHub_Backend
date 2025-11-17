package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisteredEventDto {
    
    private Integer eventId;
    private String eventName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String imageCard;
    private String status;
    private LocalDateTime registeredAt;
    private String eventRole;
    private String qrCodeUrl;
}
