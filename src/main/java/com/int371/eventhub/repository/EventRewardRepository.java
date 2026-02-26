package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.EventReward;
import com.int371.eventhub.entity.RewardStatus;

@Repository
public interface EventRewardRepository extends JpaRepository<EventReward, Integer> {
    List<EventReward> findByEventId(Integer eventId);

    List<EventReward> findByEventIdAndStatusIn(Integer eventId, List<RewardStatus> statuses);
}
