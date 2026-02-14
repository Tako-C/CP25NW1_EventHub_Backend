package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.SurveyToken;

@Repository
public interface SurveyTokenRepository extends JpaRepository<SurveyToken, String> {
    Optional<SurveyToken> findByTokenAndIsUsedFalse(String token);

    Optional<SurveyToken> findByUserIdAndEventId(Integer userId, Integer eventId);

    @Modifying
    @Query("UPDATE SurveyToken s SET s.isUsed = true WHERE s.token = :token")
    void setTokenAsUsed(@Param("token") String token);
}