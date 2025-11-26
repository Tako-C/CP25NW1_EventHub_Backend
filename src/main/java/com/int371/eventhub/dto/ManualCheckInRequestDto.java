package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class ManualCheckInRequestDto {
    private Integer userId;
    private Integer eventId;
}
