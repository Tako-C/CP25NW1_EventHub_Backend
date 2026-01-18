package com.int371.eventhub.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.int371.eventhub.dto.CreateQuestionRequestDto;
import com.int371.eventhub.dto.CreateSurveyRequestDto;
import com.int371.eventhub.dto.QuestionResponseDto;
import com.int371.eventhub.dto.SurveyGroupResponseDto;
import com.int371.eventhub.dto.SurveyResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.Question;
import com.int371.eventhub.entity.Survey;
import com.int371.eventhub.entity.SurveyStatus;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.QuestionRepository;
import com.int371.eventhub.repository.SurveyRepository;
import com.int371.eventhub.repository.UserRepository;


@Service
public class SurveyService {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ดึง Pre-Survey (Visitor & Exhibitor)
    public SurveyGroupResponseDto getPreSurveys(Integer eventId) {
        return getSurveysByTypes(eventId, SurveyType.PRE_VISITOR, SurveyType.PRE_EXHIBITOR);
    }

    // ดึง Post-Survey (Visitor & Exhibitor)
    public SurveyGroupResponseDto getPostSurveys(Integer eventId) {
        return getSurveysByTypes(eventId, SurveyType.POST_VISITOR, SurveyType.POST_EXHIBITOR);
    }

    private SurveyGroupResponseDto getSurveysByTypes(Integer eventId, SurveyType visitorType, SurveyType exhibitorType) {
        // เช็คว่ามี Event จริงไหม
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        // ดึง Survey ทั้งหมดของ Event นี้ที่ Active และอยู่ใน Type ที่ระบุ
        List<Survey> surveys = surveyRepository.findByEventIdAndStatusAndTypeIn(
            eventId, 
            SurveyStatus.ACTIVE, 
            List.of(visitorType, exhibitorType)
        );

        if (surveys.isEmpty()) {
            throw new ResourceNotFoundException("No active surveys found for event id: " + eventId);
        }

        SurveyResponseDto visitorSurvey = null;
        SurveyResponseDto exhibitorSurvey = null;

        for (Survey survey : surveys) {
            // 1. Map Survey พื้นฐาน
            SurveyResponseDto surveyDto = modelMapper.map(survey, SurveyResponseDto.class);

            // 2. ดึง Questions ของ Survey นี้
            List<Question> questions = questionRepository.findBySurveyId(survey.getId());
            
            // 3. Map Questions -> QuestionDto (รวมการจัด format choices)
            List<QuestionResponseDto> questionDtos = new ArrayList<>();
            for (Question q : questions) {
                QuestionResponseDto qDto = modelMapper.map(q, QuestionResponseDto.class);
                // จัดการรวม answer_1-5 เป็น List
                qDto.setChoicesFromAnswers(q.getAnswer1(), q.getAnswer2(), q.getAnswer3(), q.getAnswer4(), q.getAnswer5());
                questionDtos.add(qDto);
            }

            // 4. เอา List คำถามใส่กลับไปใน Survey DTO
            surveyDto.setQuestions(questionDtos);
            
            // แยกประเภทตามเดิม
            if (survey.getType() == visitorType) {
                visitorSurvey = surveyDto;
            } else if (survey.getType() == exhibitorType) {
                exhibitorSurvey = surveyDto;
            }
        }

        return new SurveyGroupResponseDto(visitorSurvey, exhibitorSurvey);
    }

    @Transactional // สำคัญ! เพื่อให้ save survey และ questions เป็น Transaction เดียวกัน ถ้า error ให้ rollback ทั้งหมด
    public SurveyResponseDto createSurvey(Integer eventId, CreateSurveyRequestDto request, String userEmail) {
        // 1. หา User จาก Email (ได้มาจาก Token)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. หา Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // 3. ตรวจสอบสิทธิ์ (Must be ORGANIZER of this event)
        MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

        if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
            throw new AccessDeniedException("Only event organizer can create surveys");
        }

        // 4. สร้างและบันทึก Survey
        Survey survey = new Survey();
        survey.setName(request.getName());
        survey.setDescription(request.getDescription());
        survey.setPoints(request.getPoints());
        survey.setType(request.getType());
        survey.setStatus(SurveyStatus.ACTIVE); // Default Active
        survey.setEvent(event);
        
        Survey savedSurvey = surveyRepository.save(survey);

        // 5. สร้างและบันทึก Questions
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questionsToSave = new ArrayList<>();
            
            for (CreateQuestionRequestDto qDto : request.getQuestions()) {
                Question q = new Question();
                q.setSurvey(savedSurvey);
                q.setQuestion(qDto.getQuestion());
                q.setQuestionType(qDto.getQuestionType());

                // Map choices (List -> Columns)
                List<String> choices = qDto.getChoices();
                if (choices != null) {
                    if (choices.size() > 0) q.setAnswer1(choices.get(0));
                    if (choices.size() > 1) q.setAnswer2(choices.get(1));
                    if (choices.size() > 2) q.setAnswer3(choices.get(2));
                    if (choices.size() > 3) q.setAnswer4(choices.get(3));
                    if (choices.size() > 4) q.setAnswer5(choices.get(4));
                }
                questionsToSave.add(q);
            }
            questionRepository.saveAll(questionsToSave);
        }

        // 6. Return ผลลัพธ์ (เรียกใช้ method getPreSurveys/PostSurveys หรือ return Dto ธรรมดา)
        // เพื่อความง่าย ผม map กลับเป็น DTO ของตัวที่เพิ่งสร้าง
        SurveyResponseDto response = modelMapper.map(savedSurvey, SurveyResponseDto.class);
        // (Optional: ถ้าต้องการ return questions กลับไปด้วย ต้อง map questions ใส่ response manually ตรงนี้)
        
        return response;
    }
}