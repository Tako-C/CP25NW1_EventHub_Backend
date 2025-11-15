package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.VisitorEvent;
import com.int371.eventhub.entity.VisitorEventId;

public interface VisitorEventRepository extends JpaRepository<VisitorEvent, VisitorEventId> {
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);
    boolean existsByUserEmailAndEventId(String email, Integer eventId);
    List<VisitorEvent> findByUserEmail(String email);
}