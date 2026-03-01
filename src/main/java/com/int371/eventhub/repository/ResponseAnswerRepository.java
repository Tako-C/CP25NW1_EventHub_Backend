package com.int371.eventhub.repository;

// import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.Survey;
// import com.int371.eventhub.entity.SurveyType;

public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, Long> {
    boolean existsByMemberEventAndQuestion_Survey(MemberEvent memberEvent, Survey survey);
    boolean existsByMemberEventId(Integer memberEventId);

    // // เช็คว่า Member คนนี้ เคยตอบคำถามที่อยู่ใน Survey ID นี้แล้วหรือยัง
    // boolean existsByMemberEventIdAndQuestion_Survey_Id(Integer memberEventId, Integer surveyId);

    // // หรือถ้าต้องการเช็คตามประเภท (เช่น POST_VISITOR)
    // boolean existsByMemberEventIdAndQuestion_Survey_TypeIn(Integer memberEventId, Collection<SurveyType> types);

}