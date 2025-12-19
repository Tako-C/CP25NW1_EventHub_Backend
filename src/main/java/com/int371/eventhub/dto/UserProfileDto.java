package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String phone;
    private String address;
    private JobDto job;
    private CountryDto country;
    private CityDto city;
    private String postCode;
    private Integer totalPoint;
    private String status;
}