package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
}