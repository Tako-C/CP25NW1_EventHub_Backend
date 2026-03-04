package com.int371.eventhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminAddUserToEventRequestDto {

    @NotNull(message = "User ID cannot be null")
    private Integer userId;
}
