package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Event;
import java.util.List;


public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByCreatedBy(Integer userId);
}
