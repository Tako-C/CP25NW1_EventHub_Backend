package com.int371.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequestDto {
    @NotBlank(message = "QR Content is required.")
    private String qrContent;
}