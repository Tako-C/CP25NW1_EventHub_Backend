package com.int371.eventhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.int371.eventhub.dto.SurveyResponseSubmissionStatusDto;
import org.springframework.data.domain.Pageable;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventStatus;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.MemberEventStatus;
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

    Integer countByEventId(Integer eventId);

    Integer countByEventIdAndStatus(Integer eventId, MemberEventStatus status);

    @Query("SELECT u.job.jobNameTh, COUNT(u) " +
            "FROM MemberEvent me JOIN me.user u " +
            "WHERE me.event.id = :eventId AND u.job IS NOT NULL " +
            "GROUP BY u.job.jobNameTh")
    List<Object[]> countOccupationsByEventId(@Param("eventId") Integer eventId);

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

    List<MemberEvent> findByEventStatusAndSendEmailIn(EventStatus status, List<Integer> sendEmail, Pageable pageable);

    @Query(value = "SELECT " +
            "    CASE " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) < 18 THEN 'ต่ำกว่า 18 ปี' " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) BETWEEN 18 AND 24 THEN '18 - 24 ปี' " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) BETWEEN 25 AND 34 THEN '25 - 34 ปี' " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) BETWEEN 35 AND 44 THEN '35 - 44 ปี' " +
            "        ELSE '45 ปีขึ้นไป' " +
            "    END AS AGE_RANGE, " +
            "    COUNT(ue.USER_ID) AS TOTAL_PARTICIPANTS " +
            "FROM USER_EVENTS ue " +
            "JOIN USERS u ON ue.USER_ID = u.ID " +
            "WHERE ue.EVENT_ID = :eventId AND u.DATE_OF_BIRTH IS NOT NULL AND ue.STATUS IN (:statuses) " +
            "GROUP BY " +
            "    CASE " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) < 18 THEN 'ต่ำกว่า 18 ปี' " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) BETWEEN 18 AND 24 THEN '18 - 24 ปี' " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) BETWEEN 25 AND 34 THEN '25 - 34 ปี' " +
            "        WHEN TRUNC(MONTHS_BETWEEN(SYSDATE, u.DATE_OF_BIRTH) / 12) BETWEEN 35 AND 44 THEN '35 - 44 ปี' " +
            "        ELSE '45 ปีขึ้นไป' " +
            "    END " +
            "ORDER BY AGE_RANGE", nativeQuery = true)
    List<Object[]> countAgeRangesByEventIdAndStatuses(@Param("eventId") Integer eventId, @Param("statuses") List<String> statuses);

    @Query(value = "SELECT " +
            "    GENDER_NAME, " +
            "    COUNT(*) AS TOTAL_USERS " +
            "FROM (" +
            "    SELECT " +
            "        CASE " +
            "            WHEN u.GENDER = 'M' THEN 'ชาย' " +
            "            WHEN u.GENDER = 'F' THEN 'หญิง' " +
            "            ELSE 'ไม่ระบุ' " +
            "        END AS GENDER_NAME " +
            "    FROM USER_EVENTS ue " +
            "    JOIN USERS u ON ue.USER_ID = u.ID " +
            "    WHERE ue.EVENT_ID = :eventId AND ue.STATUS IN (:statuses) " +
            ") " +
            "GROUP BY GENDER_NAME", nativeQuery = true)
    List<Object[]> countGendersByEventIdAndStatuses(@Param("eventId") Integer eventId, @Param("statuses") List<String> statuses);

    @Query(value = "SELECT " +
            "    j.NAME_TH AS JOB_NAME, " +
            "    COUNT(ue.USER_ID) AS TOTAL_PARTICIPANTS " +
            "FROM USER_EVENTS ue " +
            "JOIN USERS u ON ue.USER_ID = u.ID " +
            "JOIN JOBS j ON u.JOB_ID = j.ID " +
            "WHERE ue.EVENT_ID = :eventId AND ue.STATUS IN (:statuses) " +
            "GROUP BY j.NAME_TH " +
            "ORDER BY TOTAL_PARTICIPANTS DESC", nativeQuery = true)
    List<Object[]> countJobsByEventIdAndStatuses(@Param("eventId") Integer eventId, @Param("statuses") List<String> statuses);

    @Query(value = "SELECT " +
            "    c.NAME_TH AS CITY_NAME, " +
            "    COUNT(ue.USER_ID) AS TOTAL_PARTICIPANTS " +
            "FROM USER_EVENTS ue " +
            "JOIN USERS u ON ue.USER_ID = u.ID " +
            "JOIN CITIES c ON u.CITIES_ID = c.ID " +
            "WHERE ue.EVENT_ID = :eventId AND ue.STATUS IN (:statuses) " +
            "GROUP BY c.NAME_TH " +
            "ORDER BY TOTAL_PARTICIPANTS DESC", nativeQuery = true)
    List<Object[]> countCitiesByEventIdAndStatuses(@Param("eventId") Integer eventId, @Param("statuses") List<String> statuses);

    @Query(value = "SELECT " +
            "    ue.EVENT_ROLE AS ROLE_NAME, " +
            "    COUNT(ue.USER_ID) AS TOTAL_PARTICIPANTS " +
            "FROM USER_EVENTS ue " +
            "WHERE ue.EVENT_ID = :eventId AND ue.STATUS IN (:statuses) " +
            "GROUP BY ue.EVENT_ROLE " +
            "ORDER BY TOTAL_PARTICIPANTS DESC", nativeQuery = true)
    List<Object[]> countRolesByEventIdAndStatuses(@Param("eventId") Integer eventId, @Param("statuses") List<String> statuses);
}