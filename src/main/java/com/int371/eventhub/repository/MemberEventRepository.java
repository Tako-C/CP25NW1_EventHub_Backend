package com.int371.eventhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.User;

public interface MemberEventRepository extends JpaRepository<MemberEvent, Integer> {
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);
    boolean existsByUserEmailAndEventId(String email, Integer eventId);
    List<MemberEvent> findByUserEmail(String email);
    List<MemberEvent> findByEventRole(MemberEventRole role);
    Optional<MemberEvent> findByUserEmailAndEventId(String email, Integer eventId);
    // List<MemberEvent> findAllByEventRoleName(MemberEventRole roleEvent);
    List<MemberEvent> findByEventIdAndEventRole(Integer eventId, MemberEventRole eventRole);
    // List<MemberEvent> findByEventIdAndRole(Integer eventId, MemberEventRole role);
    Optional<MemberEvent> findByEventAndUser(Event event, User user);
    Optional<MemberEvent> findByUserIdAndEventId(Integer userId, Integer eventId);
}