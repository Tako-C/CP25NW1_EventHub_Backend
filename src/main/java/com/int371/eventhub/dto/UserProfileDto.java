package com.int371.eventhub.dto;

import lombok.Data;
import java.time.LocalDate;

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
    private LocalDate dateOfBirth;
    private String gender;
    private Integer totalPoint;
    private String status;
}