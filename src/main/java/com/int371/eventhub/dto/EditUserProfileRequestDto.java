package com.int371.eventhub.dto;

import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.Pattern;

@Data
public class EditUserProfileRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private JobDto job;
    private String address;
    private CountryDto country;
    private CityDto city;
    private String postCode;
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[MFUN]$", message = "Gender must be M (Male), F (Female), U (Unknown), or N (Not specified)")
    private String gender;
}
