package com.int371.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterOtpRequestDto {
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid. It should be like 'example@domain.com'.")
    private String email;

    @NotBlank(message = "First name is required.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    @NotNull(message = "Date of Birth is required.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required.")
    @Pattern(regexp = "^[MFUN]$", message = "Gender must be M (Male), F (Female), U (Unknown), or N (Not specified)")
    private String gender;
}