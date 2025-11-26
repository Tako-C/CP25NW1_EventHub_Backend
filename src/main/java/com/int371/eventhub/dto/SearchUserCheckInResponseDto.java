package com.int371.eventhub.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class SearchUserCheckInResponseDto {
    private Integer userId;
    private String name;
    private String email;
    private String phone;
    private String status;
}
