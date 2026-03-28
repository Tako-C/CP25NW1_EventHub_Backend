package com.int371.eventhub.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionResponseDto {
    private Double avgScore;
    private Double totalAvgScore;
    private List<SatisfactionKpiDto> kpi;
}
