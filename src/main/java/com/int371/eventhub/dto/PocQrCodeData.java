package com.int371.eventhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PocQrCodeData {
    private String firstName;
    private String lastName;
    private String email;
    private Integer roleId;
}