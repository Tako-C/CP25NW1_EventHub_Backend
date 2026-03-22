package com.int371.eventhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.AiEventSummaryDto;
import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.EventKpiResponseDto;
import com.int371.eventhub.dto.ResponseAnswerSearchDto;
import com.int371.eventhub.service.AiSummaryService;
import com.int371.eventhub.service.KpiService;
import com.int371.eventhub.service.ResponseAnswerService;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private KpiService kpiService;

    @Autowired
    private ResponseAnswerService responseAnswerService;

    @Autowired
    private AiSummaryService aiSummaryService;

    @PostMapping("/sync-sentiment")
    public ResponseEntity<ApiResponse<String>> syncSentiment() {
        try {
            aiSummaryService.syncSentimentAnalysis();
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Sentiment sync completed",
                    "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Sentiment sync failed: " + e.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping(value = "/summary/{eventId}", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> getEventSummary(@PathVariable Integer eventId) {
        try {
            String summaryAnalysis = aiSummaryService.getAiEventSummaryAnalysisText(eventId);
            return ResponseEntity.ok(summaryAnalysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to fetch summary: " + e.getMessage());
        }
    }

    @GetMapping("/summary-data/{eventId}")
    public ResponseEntity<ApiResponse<AiEventSummaryDto>> getEventSummaryData(@PathVariable Integer eventId) {
        try {
            AiEventSummaryDto summary = aiSummaryService.getEventSummary(eventId);
            ApiResponse<AiEventSummaryDto> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Summary data fetched successfully",
                    summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<AiEventSummaryDto> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to fetch summary data: " + e.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

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
