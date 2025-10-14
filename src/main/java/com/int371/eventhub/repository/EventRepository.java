package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {
}
