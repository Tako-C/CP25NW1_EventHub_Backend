package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import com.int371.eventhub.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateUserResponseDto {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private LocalDateTime createdAt;
    private String createdByAdminName;
}
