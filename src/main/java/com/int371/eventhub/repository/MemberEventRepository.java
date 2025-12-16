package com.int371.eventhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventId;
import com.int371.eventhub.entity.MemberEventRole;

public interface MemberEventRepository extends JpaRepository<MemberEvent, MemberEventId> {
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);
    boolean existsByUserEmailAndEventId(String email, Integer eventId);
    List<MemberEvent> findByUserEmail(String email);
    List<MemberEvent> findByEventRole(MemberEventRole role);
    Optional<MemberEvent> findByUserEmailAndEventId(String email, Integer eventId);
    // List<MemberEvent> findAllByEventRoleName(MemberEventRole roleEvent);
    List<MemberEvent> findByEventIdAndEventRole(Integer eventId, MemberEventRole eventRole);
    // List<MemberEvent> findByEventIdAndRole(Integer eventId, MemberEventRole role);
}