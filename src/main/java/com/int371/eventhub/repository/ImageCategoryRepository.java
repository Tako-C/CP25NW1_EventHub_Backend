package com.int371.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.ImageCategory;

public interface ImageCategoryRepository extends JpaRepository<ImageCategory, Integer>{

    Optional<ImageCategory> findByCategoryName(String type);
    
}
