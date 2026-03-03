package com.int371.eventhub.dto;

import java.time.LocalDate;

import com.int371.eventhub.entity.UserRole;
import com.int371.eventhub.entity.UserStatus;

import lombok.Data;

@Data
public class AdminUpdateUserRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserRole role;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private Integer jobId;
    private String address;
    private Integer countryId;
    private Integer cityId;
    private String postCode;
    private String imgPath;
    private UserStatus status;
}
