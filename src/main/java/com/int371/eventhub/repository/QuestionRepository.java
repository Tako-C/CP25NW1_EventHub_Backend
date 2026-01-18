package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // หาคำถามทั้งหมดของ Survey ID นั้นๆ
    List<Question> findBySurveyId(Integer surveyId);
}