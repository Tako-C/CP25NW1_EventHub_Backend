package com.int371.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.Survey;

public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, Long> {
    boolean existsByMemberEventAndQuestion_Survey(MemberEvent memberEvent, Survey survey);
    boolean existsByMemberEventId(Integer memberEventId);
}