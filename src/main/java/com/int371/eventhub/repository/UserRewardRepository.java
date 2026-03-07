package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.EventReward;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.entity.UserReward;

public interface UserRewardRepository extends JpaRepository<UserReward, Integer> {

    boolean existsByUserAndEventReward(User user, EventReward eventReward);

    boolean existsByEventReward(EventReward eventReward);

    List<UserReward> findByUserId(Integer userId);

    List<UserReward> findByEventRewardIn(List<EventReward> eventRewards);

    List<UserReward> findByEventReward(EventReward eventReward);

}
