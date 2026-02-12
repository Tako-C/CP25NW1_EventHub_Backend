package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class SearchUserCheckInRequestDto {
    private String query; // search by name, email, or phone
    private Integer eventId;
}
