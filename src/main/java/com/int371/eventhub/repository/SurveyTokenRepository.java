package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.SurveyToken;

@Repository
public interface SurveyTokenRepository extends JpaRepository<SurveyToken, String> {
    Optional<SurveyToken> findByTokenAndIsUsedFalse(String token);
}