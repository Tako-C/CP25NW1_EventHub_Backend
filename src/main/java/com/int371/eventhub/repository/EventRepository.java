package com.int371.eventhub.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventStatus;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByCreatedBy(Integer userId);

    List<Event> findAllByStatusNot(EventStatus status);

    Optional<Event> findByIdAndStatusNot(Integer id, EventStatus status);

    List<Event> findAllByEndDateBeforeAndStatusIn(LocalDateTime endDate, List<EventStatus> statuses);

    List<Event> findAllByStartDateBeforeAndStatus(LocalDateTime startDate, EventStatus status);

    Optional<Event> findFirstByEventNameIgnoreCase(String eventName);
}
