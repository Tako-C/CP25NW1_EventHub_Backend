package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.int371.eventhub.entity.Question;
import com.int371.eventhub.entity.Survey;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // หาคำถามทั้งหมดของ Survey ID นั้นๆ
    List<Question> findBySurveyId(Integer surveyId);

    List<Question> findBySurveyIn(List<Survey> surveys);

    @Query("SELECT q FROM Question q JOIN q.survey s WHERE s.event.id = :eventId " +
            "AND q.kpiType = 'satisfaction' AND s.type IN :surveyTypes")
    List<Question> findSatisfactionQuestionsByEventAndTypes(@Param("eventId") Integer eventId,
            @Param("surveyTypes") List<com.int371.eventhub.entity.SurveyType> surveyTypes);
}