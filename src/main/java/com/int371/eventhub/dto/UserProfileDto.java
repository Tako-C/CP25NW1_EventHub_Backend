package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private RoleDto role;
    private String phone;
    private Integer jobId;
    private String address;
    private String country;
    private String city;
    private String province;
    private String postCode;
    private Integer totalPoint;
    private Integer status;
}