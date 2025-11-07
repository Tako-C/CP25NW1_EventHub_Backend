package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.EventImage;

public interface EventImageRepository extends JpaRepository<EventImage, Integer> {
    
}
