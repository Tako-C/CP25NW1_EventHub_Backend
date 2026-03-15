package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.AiFeedbackResponseDto;
import com.int371.eventhub.dto.EventKpiResponseDto;
import com.int371.eventhub.entity.EngagementKpi;
import com.int371.eventhub.entity.OperationalKpi;
import com.int371.eventhub.entity.SatisfactionKpi;
import com.int371.eventhub.repository.AiFeedbackDataRepository;
import com.int371.eventhub.repository.EngagementKpiRepository;
import com.int371.eventhub.repository.OperationalKpiRepository;
import com.int371.eventhub.repository.SatisfactionKpiRepository;

@Service
public class KpiService {

    @Autowired
    private EngagementKpiRepository engagementRepository;

    @Autowired
    private OperationalKpiRepository operationalRepository;

    @Autowired
    private SatisfactionKpiRepository satisfactionRepository;

    @Autowired
    private AiFeedbackDataRepository aiFeedbackRepository;

    public EventKpiResponseDto getEventKpis(Integer eventId) {
        EngagementKpi engagement = engagementRepository.findByEventId(eventId).orElse(null);
        OperationalKpi operational = operationalRepository.findByEventId(eventId).orElse(null);
        SatisfactionKpi satisfaction = satisfactionRepository.findByEventId(eventId).orElse(null);

        List<AiFeedbackResponseDto> feedbacks = aiFeedbackRepository.findByEventId(eventId).stream()
                .map(data -> new AiFeedbackResponseDto(
                        data.getEventRole(),
                        data.getSurveysType(),
                        data.getFeedbackText()
                ))
                .collect(Collectors.toList());

        return new EventKpiResponseDto(engagement, operational, satisfaction, feedbacks);
    }

    public List<AiFeedbackResponseDto> getAiFeedbackByEventId(Integer eventId) {
        return aiFeedbackRepository.findByEventId(eventId).stream()
                .map(data -> new AiFeedbackResponseDto(
                        data.getEventRole(),
                        data.getSurveysType(),
                        data.getFeedbackText()
                ))
                .collect(Collectors.toList());
    }
}
