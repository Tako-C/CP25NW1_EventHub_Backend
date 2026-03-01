package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.EventReward;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.entity.UserReward;

public interface UserRewardRepository extends JpaRepository<UserReward, Integer> {

    boolean existsByUserAndEventReward(User user, EventReward eventReward);

    List<UserReward> findByUserId(Integer userId);

}
