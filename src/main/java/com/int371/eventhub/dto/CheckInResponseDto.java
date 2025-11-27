package com.int371.eventhub.dto;


import lombok.Data;
@Data
public class CheckInResponseDto {
    private String userId;
    private String userName;
    private String email;
    private String phone;
    private String eventName;
}
