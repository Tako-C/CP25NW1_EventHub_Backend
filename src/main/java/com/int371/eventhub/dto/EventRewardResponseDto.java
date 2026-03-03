package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import com.int371.eventhub.entity.RewardRequirementType;
import com.int371.eventhub.entity.RewardStatus;

import lombok.Data;

@Data
public class EventRewardResponseDto {
    private Integer id;
    private String name;
    private String description;
    private Integer eventId;
    private RewardRequirementType requirementType;
    private LocalDateTime startRedeemAt;
    private LocalDateTime endRedeemAt;
    private Integer quantity;
    private RewardStatus status;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean eligible;
}
