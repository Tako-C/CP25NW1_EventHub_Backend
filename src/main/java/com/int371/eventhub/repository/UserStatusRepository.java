package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.UserStatus;

public interface UserStatusRepository extends JpaRepository<UserStatus, Integer> {
    
}
