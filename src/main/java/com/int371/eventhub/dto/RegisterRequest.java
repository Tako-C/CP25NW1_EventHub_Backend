package com.int371.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email(message = "Email format is not valid. It should be like 'example@domain.com'.")
    private String email;

    @NotBlank
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    private String password;
}