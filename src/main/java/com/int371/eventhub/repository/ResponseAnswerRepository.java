package com.int371.eventhub.repository;

import java.util.List;

// import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.Survey;

public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, Long> {
    boolean existsByMemberEventAndQuestion_Survey(MemberEvent memberEvent, Survey survey);

    boolean existsByQuestion_Survey(Survey survey);

    boolean existsByMemberEventId(Integer memberEventId);

    java.util.List<ResponseAnswer> findByMemberEventIn(List<MemberEvent> memberEvents);

    java.util.List<ResponseAnswer> findByQuestionIn(List<com.int371.eventhub.entity.Question> questions);

    java.util.List<ResponseAnswer> findByKeywordContainingIgnoreCaseAndQuestionType(String keyword,
            QuestionType questionType);

}