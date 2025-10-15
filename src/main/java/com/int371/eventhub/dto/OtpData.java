package com.int371.eventhub.dto;

import lombok.Value;

@Value
public class OtpData {
    String otp;
    String firstName;
    String lastName;
    String password;
}