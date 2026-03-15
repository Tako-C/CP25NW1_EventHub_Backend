package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.SatisfactionKpi;

@Repository
public interface SatisfactionKpiRepository extends JpaRepository<SatisfactionKpi, Integer> {
    Optional<SatisfactionKpi> findByEventId(Integer eventId);
}
