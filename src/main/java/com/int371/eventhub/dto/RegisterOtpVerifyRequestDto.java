package com.int371.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterOtpVerifyRequestDto {
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid. It should be like 'example@domain.com'.")
    private String email;

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotBlank(message = "Password is required.")
    private String password;
}
