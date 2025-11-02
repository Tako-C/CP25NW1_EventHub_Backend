package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class StatusDto {
    private Integer id;
    private String statusName;
    private String statusDescription;
}