package com.int371.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EventRegisterRequestDto {

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid. It should be like 'example@domain.com'.")
    private String email;

    @NotBlank(message = "First name is required.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotNull(message = "Date of birth is required.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required.")
    @Pattern(regexp = "^[MFUN]$", message = "Gender must be M (Male), F (Female), U (Unknown), or N (Not specified)")
    private String gender;
}
