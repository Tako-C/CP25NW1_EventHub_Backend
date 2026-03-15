package com.int371.eventhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.EventKpiResponseDto;
import com.int371.eventhub.dto.ResponseAnswerSearchDto;
import com.int371.eventhub.service.KpiService;
import com.int371.eventhub.service.ResponseAnswerService;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private KpiService kpiService;

    @Autowired
    private ResponseAnswerService responseAnswerService;

    @GetMapping("/kpi/events/{eventId}")
    public ResponseEntity<ApiResponse<EventKpiResponseDto>> getEventKpis(@PathVariable Integer eventId) {
        EventKpiResponseDto kpis = kpiService.getEventKpis(eventId);
        ApiResponse<EventKpiResponseDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Event KPIs fetched successfully",
                kpis);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/answers/search")
    public ResponseEntity<ApiResponse<List<ResponseAnswerSearchDto>>> searchAnswersByKeyword(
            @RequestParam String keyword) {

        List<ResponseAnswerSearchDto> data = responseAnswerService.searchAnswersByKeyword(keyword);

        ApiResponse<List<ResponseAnswerSearchDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Answers fetched successfully",
                data);

        return ResponseEntity.ok(response);
    }
}
