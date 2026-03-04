package com.int371.eventhub.dto;

import com.int371.eventhub.entity.MemberEventRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserRoleInEventRequestDto {

    @NotNull(message = "Role cannot be null")
    private MemberEventRole role;
}
