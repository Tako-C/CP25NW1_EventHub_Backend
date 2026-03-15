package com.int371.eventhub.dto;

import java.util.List;

import com.int371.eventhub.entity.EngagementKpi;
import com.int371.eventhub.entity.OperationalKpi;
import com.int371.eventhub.entity.SatisfactionKpi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventKpiResponseDto {
    private EngagementKpi engagement;
    private OperationalKpi operational;
    private SatisfactionKpi satisfaction;
    private List<AiFeedbackResponseDto> feedbacks;
}
