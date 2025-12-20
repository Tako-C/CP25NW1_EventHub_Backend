package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.City;

public interface CityRepository extends JpaRepository<City, Integer> {

    List<City> findByCountryId(Integer countryId);
    
}
