package com.int371.eventhub.dto;

import java.time.LocalDateTime;


import lombok.Data;

@Data
public class ListCheckInResponseDto {
    private String name;
    private String email;
    private String phone;
    private LocalDateTime registration_date;
    private String status;
}
