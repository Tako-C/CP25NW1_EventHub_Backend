package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.MemberEventRoleName;

public interface MemberEventRoleRepository extends JpaRepository<MemberEventRole, Integer> {
    Optional<MemberEventRole> findByName(MemberEventRoleName name);

}