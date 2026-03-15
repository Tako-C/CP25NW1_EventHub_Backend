package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.EngagementKpi;

@Repository
public interface EngagementKpiRepository extends JpaRepository<EngagementKpi, Integer> {
    Optional<EngagementKpi> findByEventId(Integer eventId);
}
