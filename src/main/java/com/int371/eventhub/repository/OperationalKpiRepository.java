package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.OperationalKpi;

@Repository
public interface OperationalKpiRepository extends JpaRepository<OperationalKpi, Integer> {
    Optional<OperationalKpi> findByEventId(Integer eventId);
}
