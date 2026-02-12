package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}