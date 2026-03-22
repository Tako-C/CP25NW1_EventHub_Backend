package com.int371.eventhub.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.int371.eventhub.dto.AiEventSummaryDto;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.dto.SentimentAnalyzeRequestDto;
import com.int371.eventhub.dto.SentimentAnalyzeResponseDto;
import com.int371.eventhub.dto.SentimentCategoryDto;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.SuggestionsAnalysis;
import com.int371.eventhub.repository.ResponseAnswerRepository;
import com.int371.eventhub.repository.SuggestionsAnalysisRepository;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.SatisfactionKpiRepository;
import com.int371.eventhub.repository.EngagementKpiRepository;
import com.int371.eventhub.repository.AiEventAnalysisRepository;
import com.int371.eventhub.entity.AiEventAnalysis;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EngagementKpi;
import com.int371.eventhub.entity.SatisfactionKpi;

@Service
public class AiSummaryService {

    private final RestClient restClient;

    @Autowired
    private ResponseAnswerRepository responseAnswerRepository;

    @Autowired
    private SuggestionsAnalysisRepository suggestionsAnalysisRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired
    private SatisfactionKpiRepository satisfactionKpiRepository;

    @Autowired
    private EngagementKpiRepository engagementKpiRepository;

    @Autowired
    private AiEventAnalysisRepository aiEventAnalysisRepository;

    public AiSummaryService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://cp25nw1.sit.kmutt.ac.th:8000")
                .build();
    }

    @Scheduled(fixedDelay = 300000) // Runs automatically every 5 minutes
    public void syncSentimentAnalysis() {
        List<ResponseAnswer> textAnswers = responseAnswerRepository.findByQuestionType(QuestionType.TEXT);

        List<SentimentAnalyzeRequestDto> requestBatch = new java.util.ArrayList<>();
        Map<Long, ResponseAnswer> answerMap = new java.util.HashMap<>();

        for (ResponseAnswer answer : textAnswers) {
            String suggestionText = answer.getAnswer();
            if (suggestionText == null || suggestionText.trim().isEmpty()) {
                continue;
            }
            if (!suggestionsAnalysisRepository.existsByResponseAnswerId(answer.getId())) {
                requestBatch.add(new SentimentAnalyzeRequestDto(String.valueOf(answer.getId()), suggestionText));
                answerMap.put(answer.getId(), answer);
            }
        }

        if (requestBatch.isEmpty()) {
            return;
        }

        try {
            String rawJson = restClient.post()
                    .uri("/analyze-suggestions")
                    .body(requestBatch)
                    .retrieve()
                    .body(String.class);

            System.out.println("Raw API Response: " + rawJson);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            JsonNode dataNode = root.get("data");

            if (dataNode != null && dataNode.isArray()) {
                SentimentAnalyzeResponseDto[] responses = mapper.treeToValue(dataNode,
                        SentimentAnalyzeResponseDto[].class);

                if (responses.length == 0 && !requestBatch.isEmpty()) {
                    throw new RuntimeException(
                            "AI API returned an empty data array. (AI ไม่คืนค่าการกรองข้อความกลับมา)");
                }

                for (int i = 0; i < responses.length; i++) {
                    SentimentAnalyzeResponseDto response = responses[i];
                    Long rsId = response.getRsId() != null
                            ? Long.parseLong(response.getRsId())
                            : Long.parseLong(requestBatch.get(i).getRsId());
                    ResponseAnswer answer = answerMap.get(rsId);

                    if (answer != null && response.getSentiment() != null) {
                        SuggestionsAnalysis analysis = new SuggestionsAnalysis();
                        analysis.setResponseAnswer(answer);
                        analysis.setSentiment(response.getSentiment().trim().toUpperCase());
                        analysis.setKeyword(response.getKeyword());
                        suggestionsAnalysisRepository.save(analysis);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to sync sentiment in batch mode.");
            e.printStackTrace();
            throw new RuntimeException("API Sync Error: " + e.getMessage(), e);
        }
    }

    private String getOrDefaultString(String value) {
        return (value == null || value.trim().isEmpty()) ? "ไม่มีข้อมูล" : value;
    }

    public AiEventSummaryDto getEventSummary(Integer eventId) {
        AiEventSummaryDto summary = new AiEventSummaryDto();

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event != null) {
            summary.setEventName(getOrDefaultString(event.getEventName()));
            summary.setEventType(
                    event.getEventTypeId() != null ? getOrDefaultString(event.getEventTypeId().getEventTypeName())
                            : "ไม่มีข้อมูล");
            summary.setLocation(getOrDefaultString(event.getLocation()));
            summary.setEventDetail(getOrDefaultString(event.getEventDesc()));
        } else {
            summary.setEventName("ไม่มีข้อมูล");
            summary.setEventType("ไม่มีข้อมูล");
            summary.setLocation("ไม่มีข้อมูล");
            summary.setEventDetail("ไม่มีข้อมูล");
        }

        EngagementKpi engKpi = engagementKpiRepository.findById(eventId).orElse(null);
        if (engKpi != null) {
            summary.setTotalRegistered(engKpi.getTotalRegistered() != null ? engKpi.getTotalRegistered() : 0);
            summary.setTotalCheckedIn(engKpi.getTotalCheckedIn() != null ? engKpi.getTotalCheckedIn() : 0);
        } else {
            summary.setTotalRegistered(0);
            summary.setTotalCheckedIn(0);
        }

        Integer totalFeedback = responseAnswerRepository.countTextFeedbackByEventId(eventId);
        summary.setTotalFeedback(totalFeedback != null ? totalFeedback : 0);

        List<Object[]> occList = memberEventRepository.countOccupationsByEventId(eventId);
        Map<String, Integer> occupations = new HashMap<>();
        for (Object[] occ : occList) {
            String jobName = (String) occ[0];
            Long count = occ[1] != null ? (Long) occ[1] : 0L;
            String jobKey = (jobName != null && !jobName.trim().isEmpty()) ? jobName : "ไม่มีข้อมูล";
            occupations.put(jobKey, count.intValue());
        }
        summary.setOccupations(occupations);

        SatisfactionKpi kpi = satisfactionKpiRepository.findById(eventId).orElse(null);
        if (kpi != null) {
            summary.setVisitorScore(kpi.getVisitorAvgScore() != null ? kpi.getVisitorAvgScore() : 0.0);
            summary.setExhibitorScore(kpi.getExhibitorAvgScore() != null ? kpi.getExhibitorAvgScore() : 0.0);
        } else {
            summary.setVisitorScore(0.0);
            summary.setExhibitorScore(0.0);
        }

        summary.setReturningVisitorRate(0.0); // Placeholder unless logic to compute returning visitors exists

        List<SentimentCategoryDto> negVisitor = suggestionsAnalysisRepository.findTopAnalysisByEventAndSentimentAndRole(
                eventId, "NEGATIVE", MemberEventRole.VISITOR, PageRequest.of(0, 3));
        List<SentimentCategoryDto> negExhibitor = suggestionsAnalysisRepository.findTopAnalysisByEventAndSentimentAndRole(
                eventId, "NEGATIVE", MemberEventRole.EXHIBITOR, PageRequest.of(0, 3));
        
        List<SentimentCategoryDto> topIssues = new java.util.ArrayList<>();
        if (negVisitor != null) topIssues.addAll(negVisitor);
        if (negExhibitor != null) topIssues.addAll(negExhibitor);
        summary.setTopIssues(topIssues.isEmpty() ? "ไม่มีข้อมูล" : topIssues);

        List<SentimentCategoryDto> posVisitor = suggestionsAnalysisRepository.findTopAnalysisByEventAndSentimentAndRole(
                eventId, "POSITIVE", MemberEventRole.VISITOR, PageRequest.of(0, 3));
        List<SentimentCategoryDto> posExhibitor = suggestionsAnalysisRepository.findTopAnalysisByEventAndSentimentAndRole(
                eventId, "POSITIVE", MemberEventRole.EXHIBITOR, PageRequest.of(0, 3));
                
        List<SentimentCategoryDto> topGood = new java.util.ArrayList<>();
        if (posVisitor != null) topGood.addAll(posVisitor);
        if (posExhibitor != null) topGood.addAll(posExhibitor);
        summary.setTopGood(topGood.isEmpty() ? "ไม่มีข้อมูล" : topGood);

        return summary;
    }

    public String getAiEventSummaryAnalysisText(Integer eventId) {
        AiEventSummaryDto summary = getEventSummary(eventId);

        try {
            String rawJson = restClient.post()
                    .uri("/analyze-event-performance")
                    .body(summary)
                    .retrieve()
                    .body(String.class);

            if (rawJson == null) {
                rawJson = "";
            }

            AiEventAnalysis analysis = new AiEventAnalysis();
            analysis.setEventId(eventId);
            analysis.setRawJsonResult(rawJson);
            analysis.setModelVersion("gpt-4o-mini");

            String[] parts = rawJson.split("---", 2);
            if (parts.length >= 2) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode ratesNode = mapper.readTree(parts[0].trim());
                    if (ratesNode.has("check_in_rate")) {
                        analysis.setCheckInRate(ratesNode.get("check_in_rate").asDouble());
                    }
                    if (ratesNode.has("survey_rate")) {
                        analysis.setSurveyRate(ratesNode.get("survey_rate").asDouble());
                    }
                } catch (Exception e) {
                    // Ignore JSON parsing errors
                }
            } else {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode ratesNode = mapper.readTree(rawJson);
                    if (ratesNode.has("check_in_rate")) {
                        analysis.setCheckInRate(ratesNode.get("check_in_rate").asDouble());
                    }
                    if (ratesNode.has("survey_rate")) {
                        analysis.setSurveyRate(ratesNode.get("survey_rate").asDouble());
                    }
                } catch (Exception e) {
                    // Ignore JSON parsing errors
                }
            }

            try {
                aiEventAnalysisRepository.save(analysis);
            } catch (Exception e) {
                System.err.println("Warning: Could not save AiEventAnalysis to DB: " + e.getMessage());
            }

            return rawJson;
        } catch (Exception e) {
            System.err.println("Failed to get AI event performance analysis.");
            e.printStackTrace();
            throw new RuntimeException("API Analysis Error: " + e.getMessage(), e);
        }
    }
}
