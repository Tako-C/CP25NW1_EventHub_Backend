package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.SuggestionsAnalysis;

public interface SuggestionsAnalysisRepository extends JpaRepository<SuggestionsAnalysis, Long> {

       boolean existsByResponseAnswerId(Long responseAnswerId);

       @Query("SELECT s FROM SuggestionsAnalysis s JOIN FETCH s.responseAnswer r " +
                     "WHERE LOWER(s.keyword) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                     "AND r.questionType = :questionType")
       List<SuggestionsAnalysis> findByKeywordContainingIgnoreCaseAndResponseAnswerQuestionType(
                     @Param("keyword") String keyword,
                     @Param("questionType") QuestionType questionType);

       @Query("SELECT new com.int371.eventhub.dto.SentimentCategoryDto( " +
                     "s.keyword, " +
                     "CAST(COUNT(s.id) AS int), " +
                     "CAST(r.memberEvent.eventRole AS string), " +
                     "CAST(MAX(r.answer) AS string), " +
                     "s.sentiment) " +
                     "FROM SuggestionsAnalysis s " +
                     "JOIN s.responseAnswer r " +
                     "JOIN r.question q " +
                     "JOIN q.survey su " +
                     "WHERE su.event.id = :eventId AND s.sentiment = :sentiment AND s.keyword IS NOT NULL " +
                     "GROUP BY s.keyword, r.memberEvent.eventRole, s.sentiment " +
                     "ORDER BY COUNT(s.id) DESC")
       List<com.int371.eventhub.dto.SentimentCategoryDto> findTopAnalysisByEventAndSentiment(
                     @Param("eventId") Integer eventId,
                     @Param("sentiment") String sentiment,
                     Pageable pageable);

       @Query("SELECT new com.int371.eventhub.dto.SentimentCategoryDto( " +
                     "s.keyword, " +
                     "CAST(COUNT(s.id) AS int), " +
                     "CAST(r.memberEvent.eventRole AS string), " +
                     "CAST(MAX(r.answer) AS string), " +
                     "s.sentiment) " +
                     "FROM SuggestionsAnalysis s " +
                     "JOIN s.responseAnswer r " +
                     "JOIN r.question q " +
                     "JOIN q.survey su " +
                     "WHERE su.event.id = :eventId AND s.sentiment = :sentiment AND s.keyword IS NOT NULL " +
                     "AND r.memberEvent.eventRole = :role " +
                     "GROUP BY s.keyword, r.memberEvent.eventRole, s.sentiment " +
                     "ORDER BY COUNT(s.id) DESC")
       List<com.int371.eventhub.dto.SentimentCategoryDto> findTopAnalysisByEventAndSentimentAndRole(
                     @Param("eventId") Integer eventId,
                     @Param("sentiment") String sentiment,
                     @Param("role") com.int371.eventhub.entity.MemberEventRole role,
                     Pageable pageable);

       void deleteByResponseAnswerIn(List<ResponseAnswer> responseAnswers);
}
