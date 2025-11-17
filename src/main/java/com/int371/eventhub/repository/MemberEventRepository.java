package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventId;

public interface MemberEventRepository extends JpaRepository<MemberEvent, MemberEventId> {
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);
    boolean existsByUserEmailAndEventId(String email, Integer eventId);
    List<MemberEvent> findByUserEmail(String email);
}