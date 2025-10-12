package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private int statusCode;
    private String message;
    private T data;
    private String error;
    private String path;

    // Constructor สำหรับ Success Response
    public ApiResponse(int statusCode, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    // Constructor สำหรับ Error Response
    public ApiResponse(int statusCode, String message, String error) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
    }

    // Constructor สำหรับ Error ที่มี Path
    public ApiResponse(int statusCode, String message, String error, String path) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
        this.path = path;
    }
}