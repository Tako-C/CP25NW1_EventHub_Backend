package com.int371.eventhub.dto;

import java.time.LocalDateTime;

import com.int371.eventhub.entity.RewardRequirementType;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateEventRewardRequestDto {
    private String name;
    private String description;
    private RewardRequirementType requirementType;
    private LocalDateTime startRedeemAt;
    private LocalDateTime endRedeemAt;
    private Integer quantity;
    private MultipartFile image;
}
