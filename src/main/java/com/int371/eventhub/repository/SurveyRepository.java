package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Survey;
import com.int371.eventhub.entity.SurveyStatus;
import com.int371.eventhub.entity.SurveyType;

public interface SurveyRepository extends JpaRepository<Survey, Integer> {
    List<Survey> findByEventIdAndStatus(Integer eventId, SurveyStatus status);
    List<Survey> findByEventIdAndStatusAndTypeIn(Integer eventId, SurveyStatus status, List<SurveyType> types);
}