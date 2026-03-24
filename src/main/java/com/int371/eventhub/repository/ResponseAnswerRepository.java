package com.int371.eventhub.repository;

import java.util.List;

// import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.SurveyType;
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

    @Query(value = "SELECT TO_CHAR(ra.CREATED_AT, 'HH24') AS HOUR, COUNT(DISTINCT ra.USER_EVENT_ID || '-' || q.SURVEY_ID) " +
            "FROM RESPONSE_ANSWER ra " +
            "JOIN QUESTIONS q ON ra.QUESTION_ID = q.ID " +
            "JOIN SURVEYS s ON q.SURVEY_ID = s.ID " +
            "JOIN USER_EVENTS ue ON ra.USER_EVENT_ID = ue.ID " +
            "WHERE s.EVENT_ID = :eventId AND UPPER(ue.EVENT_ROLE) = UPPER(:role) " +
            "AND s.SURVEYS_TYPE IN ('POST_VISITOR', 'POST_EXHIBITOR') " +
            "GROUP BY TO_CHAR(ra.CREATED_AT, 'HH24') " +
            "ORDER BY HOUR", nativeQuery = true)
    List<Object[]> countHourlySubmissionsByEventIdAndRole(@Param("eventId") Integer eventId,
            @Param("role") String role);

    @Query("SELECT ra.answer, COUNT(ra) FROM ResponseAnswer ra " +
            "JOIN ra.question q JOIN q.survey s " +
            "WHERE s.event.id = :eventId AND UPPER(q.kpiType) = 'SATISFACTION' " +
            "AND s.type IN :surveyTypes " +
            "GROUP BY ra.answer")
    List<Object[]> countSatisfactionByEventAndSurveyTypes(@Param("eventId") Integer eventId,
            @Param("surveyTypes") List<SurveyType> surveyTypes);

    @Query("SELECT me.eventRole, ra.answer, s.type, sa.keyword, sa.sentiment " +
            "FROM ResponseAnswer ra " +
            "JOIN ra.memberEvent me " +
            "JOIN ra.question q " +
            "JOIN q.survey s " +
            "LEFT JOIN SuggestionsAnalysis sa ON sa.responseAnswer = ra " +
            "WHERE s.event.id = :eventId AND ra.questionType = com.int371.eventhub.entity.QuestionType.TEXT")
    List<Object[]> findFullTextResponsesByEventId(@Param("eventId") Integer eventId);
}