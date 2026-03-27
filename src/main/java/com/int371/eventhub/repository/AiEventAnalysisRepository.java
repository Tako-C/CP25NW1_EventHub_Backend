package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.int371.eventhub.entity.AiEventAnalysis;

public interface AiEventAnalysisRepository extends JpaRepository<AiEventAnalysis, Long> {
    List<AiEventAnalysis> findByEventIdOrderByCreatedAtDesc(Integer eventId);
}
