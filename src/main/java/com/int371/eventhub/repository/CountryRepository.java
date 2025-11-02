package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.Country;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    
}
