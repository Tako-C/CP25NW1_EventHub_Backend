package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class RedeemRewardRequest {

    private Integer userId;
    private Integer eventRewardId;

}
