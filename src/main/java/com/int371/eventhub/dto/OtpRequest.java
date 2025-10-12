package com.int371.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank
    @Email(message = "Email format is not valid. It should be like 'example@domain.com'.")
    private String email;
}