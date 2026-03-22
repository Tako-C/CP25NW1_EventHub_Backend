package com.int371.eventhub.repository;

import java.util.List;

// import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.Survey;

public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, Long> {
    boolean existsByMemberEventAndQuestion_Survey(MemberEvent memberEvent, Survey survey);

    boolean existsByQuestion_Survey(Survey survey);

    boolean existsByMemberEventId(Integer memberEventId);

    List<ResponseAnswer> findByMemberEventIn(List<MemberEvent> memberEvents);

    List<ResponseAnswer> findByQuestionIn(List<com.int371.eventhub.entity.Question> questions);

    List<ResponseAnswer> findByQuestionType(QuestionType questionType);

    @Query("SELECT COUNT(r) FROM ResponseAnswer r JOIN r.question q JOIN q.survey su WHERE su.event.id = :eventId AND r.questionType = com.int371.eventhub.entity.QuestionType.TEXT")
    Integer countTextFeedbackByEventId(@Param("eventId") Integer eventId);

}