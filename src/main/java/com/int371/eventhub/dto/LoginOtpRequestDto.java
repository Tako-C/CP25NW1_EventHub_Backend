package com.int371.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginOtpRequestDto {
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid. It should be like 'example@domain.com'.")
    private String email;
}