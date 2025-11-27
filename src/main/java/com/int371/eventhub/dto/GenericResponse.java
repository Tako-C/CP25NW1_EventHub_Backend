package com.int371.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse<T> {
    private int statuscode;
    private String message;
    private T data;

    // Constructor สำหรับกรณีสำเร็จ
    public GenericResponse(String message, T data) {
        this.statuscode = 200;
        this.message = message;
        this.data = data;
    }
}