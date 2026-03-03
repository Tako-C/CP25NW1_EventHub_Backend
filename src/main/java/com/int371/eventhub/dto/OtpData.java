package com.int371.eventhub.dto;

import lombok.Value;
import java.time.LocalDate;

@Value
public class OtpData {
    String otp;
    String firstName;
    String lastName;
    String password;
    LocalDate dateOfBirth;
    String gender;
}