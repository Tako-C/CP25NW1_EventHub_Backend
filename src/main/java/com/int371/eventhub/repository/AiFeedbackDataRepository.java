package com.int371.eventhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.int371.eventhub.entity.AiFeedbackData;
import com.int371.eventhub.entity.AiFeedbackDataId;

@Repository
public interface AiFeedbackDataRepository extends JpaRepository<AiFeedbackData, AiFeedbackDataId> {
    List<AiFeedbackData> findByEventId(Integer eventId);
}
