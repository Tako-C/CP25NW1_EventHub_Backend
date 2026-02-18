package com.int371.eventhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.int371.eventhub.dto.SurveyResponseSubmissionStatusDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.User;

public interface MemberEventRepository extends JpaRepository<MemberEvent, Integer> {
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);

    boolean existsByUserEmailAndEventId(String email, Integer eventId);

    List<MemberEvent> findByUserEmail(String email);

    List<MemberEvent> findByEventRole(MemberEventRole role);

    Optional<MemberEvent> findByUserEmailAndEventId(String email, Integer eventId);

    // List<MemberEvent> findAllByEventRoleName(MemberEventRole roleEvent);
    List<MemberEvent> findByEventIdAndEventRole(Integer eventId, MemberEventRole eventRole);

    // List<MemberEvent> findByEventIdAndRole(Integer eventId, MemberEventRolerole);
    Optional<MemberEvent> findByEventAndUser(Event event, User user);

    Optional<MemberEvent> findByUserIdAndEventId(Integer userId, Integer eventId);

    List<MemberEvent> findByEventId(Integer eventId);

    @Query("""
                SELECT new com.int371.eventhub.dto.SurveyResponseSubmissionStatusDto(
                    me.id,
                    u.firstName,
                    u.lastName,
                    e.eventName,
                    s.type,
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM ResponseAnswer ra
                            WHERE ra.memberEvent = me
                            AND ra.question.survey.id = :surveyId
                        )
                        THEN true
                        ELSE false
                    END
                )
                FROM MemberEvent me
                JOIN me.user u
                JOIN me.event e
                JOIN Survey s ON s.id = :surveyId
                WHERE e.id = :eventId
                AND me.eventRole = :role
            """)
    List<SurveyResponseSubmissionStatusDto> findSurveySubmissionStatusByRole(
            Integer eventId,
            Integer surveyId,
            MemberEventRole role);

    @Query("SELECT me FROM MemberEvent me JOIN me.user u WHERE " +
            "me.event.id = :eventId AND me.eventRole = MemberEventRole.VISITOR AND (" +
            "LOWER(u.email) = LOWER(:query) OR " +
            "u.phone = :query OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<MemberEvent> searchVisitorsFlexibly(@Param("eventId") Integer eventId, @Param("query") String query);

    List<MemberEvent> findByEventIdAndSendEmail(Integer eventId, Integer sendEmail);
}