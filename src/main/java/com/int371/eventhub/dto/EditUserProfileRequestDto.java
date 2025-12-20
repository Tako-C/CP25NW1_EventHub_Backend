package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class EditUserProfileRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private JobDto job;
    private String address;
    private CountryDto country;
    private CityDto city;
    private String postCode;
}
