package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberEventQrData {
    private Integer userId;
    private Integer eventId;
    private String registrationDate;
    private String status; 
}