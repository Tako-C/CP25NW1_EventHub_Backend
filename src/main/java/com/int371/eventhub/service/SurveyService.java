package com.int371.eventhub.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.int371.eventhub.dto.CreateQuestionRequestDto;
import com.int371.eventhub.dto.CreateSurveyRequestDto;
import com.int371.eventhub.dto.QuestionResponseDto;
import com.int371.eventhub.dto.SurveyAnswerRequestDto;
import com.int371.eventhub.dto.SurveyGroupResponseDto;
import com.int371.eventhub.dto.SurveyResponseDto;
import com.int371.eventhub.dto.SurveySubmissionRequestDto;
import com.int371.eventhub.dto.UpdateQuestionDto;
import com.int371.eventhub.dto.UpdateSurveyRequestDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.Question;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.Survey;
import com.int371.eventhub.entity.SurveyStatus;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.QuestionRepository;
import com.int371.eventhub.repository.ResponseAnswerRepository;
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

    @Autowired
    private ResponseAnswerRepository responseAnswerRepository;

    private static final int MAX_PRE_SURVEY_QUESTIONS = 5;
    private static final int MAX_POST_SURVEY_QUESTIONS = 10;
    private static final int MIN_CHOICES = 3;
    private static final int MAX_CHOICES = 5;

    // ดึง Pre-Survey
    public SurveyGroupResponseDto getPreSurveys(Integer eventId) {
        return getSurveysByTypes(eventId, SurveyType.PRE_VISITOR, SurveyType.PRE_EXHIBITOR);
    }

    // ดึง Post-Survey
    public SurveyGroupResponseDto getPostSurveys(Integer eventId) {
        return getSurveysByTypes(eventId, SurveyType.POST_VISITOR, SurveyType.POST_EXHIBITOR);
    }

    private SurveyGroupResponseDto getSurveysByTypes(Integer eventId, SurveyType visitorType, SurveyType exhibitorType) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

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
            SurveyResponseDto surveyDto = modelMapper.map(survey, SurveyResponseDto.class);

            List<Question> questions = questionRepository.findBySurveyId(survey.getId());
            
            List<QuestionResponseDto> questionDtos = new ArrayList<>();
            for (Question q : questions) {
                QuestionResponseDto qDto = modelMapper.map(q, QuestionResponseDto.class);
                qDto.setChoicesFromAnswers(q.getAnswer1(), q.getAnswer2(), q.getAnswer3(), q.getAnswer4(), q.getAnswer5());
                questionDtos.add(qDto);
            }

            surveyDto.setQuestions(questionDtos);
            surveyDto.setCreatedAt(survey.getCreatedAt());
            surveyDto.setUpdatedAt(survey.getUpdatedAt());
            
            if (survey.getType() == visitorType) {
                visitorSurvey = surveyDto;
            } else if (survey.getType() == exhibitorType) {
                exhibitorSurvey = surveyDto;
            }
        }

        return new SurveyGroupResponseDto(visitorSurvey, exhibitorSurvey);
    }

    @Transactional
    public SurveyResponseDto createSurvey(Integer eventId, CreateSurveyRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (surveyRepository.existsByEventIdAndType(eventId, request.getSurveyType())) {
            throw new IllegalArgumentException("Survey of type " + request.getSurveyType() + " already exists for this event.");
        }

        validateQuestionCount(request.getSurveyType(), request.getQuestions().size());

        validateQuestionContent(request.getQuestions());

        MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

        if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
            throw new AccessDeniedException("Only event organizer can create surveys");
        }


        Survey survey = new Survey();
        survey.setName(request.getName());
        survey.setDescription(request.getDescription());
        survey.setPoints(request.getPoints());
        survey.setType(request.getSurveyType());
        survey.setStatus(SurveyStatus.ACTIVE); // Default Active
        survey.setEvent(event);
        
        Survey savedSurvey = surveyRepository.save(survey);

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
                    if (!choices.isEmpty()) q.setAnswer1(choices.get(0));
                    if (choices.size() > 1) q.setAnswer2(choices.get(1));
                    if (choices.size() > 2) q.setAnswer3(choices.get(2));
                    if (choices.size() > 3) q.setAnswer4(choices.get(3));
                    if (choices.size() > 4) q.setAnswer5(choices.get(4));
                }
                questionsToSave.add(q);
            }
            questionRepository.saveAll(questionsToSave);
        }

        SurveyResponseDto response = modelMapper.map(savedSurvey, SurveyResponseDto.class);
        return response;
    }

    @Transactional
    public SurveyResponseDto updateSurvey(Integer eventId, Integer surveyId, UpdateSurveyRequestDto request, String userEmail) {
        checkOrganizerPermission(eventId, userEmail);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + surveyId));

        if (!survey.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Survey id " + surveyId + " does not belong to event id " + eventId);
        }

        survey.setName(request.getName());
        survey.setDescription(request.getDescription());
        survey.setPoints(request.getPoints());
        survey.setType(request.getSurveyType());

        Survey savedSurvey = surveyRepository.save(survey);

        if (request.getQuestions() != null) {
            List<Question> existingQuestions = questionRepository.findBySurveyId(surveyId);
            Map<Integer, Question> existingMap = existingQuestions.stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));

            List<Question> questionsToSave = new ArrayList<>();
            List<Integer> incomingIds = new ArrayList<>();

            for (UpdateQuestionDto qDto : request.getQuestions()) {
                Question question;

                if (qDto.getId() != null && existingMap.containsKey(qDto.getId())) {
                    question = existingMap.get(qDto.getId());
                    incomingIds.add(qDto.getId());
                } else {
                    question = new Question();
                    question.setSurvey(savedSurvey);
                }

                question.setQuestion(qDto.getQuestion());
                question.setQuestionType(qDto.getQuestionType());
                
                mapChoicesToQuestion(question, qDto.getChoices());

                questionsToSave.add(question);
            }

            List<Question> questionsToDelete = existingQuestions.stream()
                    .filter(q -> !incomingIds.contains(q.getId()))
                    .collect(Collectors.toList());

            questionRepository.deleteAll(questionsToDelete);
            questionRepository.saveAll(questionsToSave);
        }

        return modelMapper.map(savedSurvey, SurveyResponseDto.class);
    }

    @Transactional
    public void deleteSurvey(Integer eventId, Integer surveyId, String userEmail) {
        checkOrganizerPermission(eventId, userEmail);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + surveyId));

        if (!survey.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Survey does not belong to this event");
        }

        List<Question> questions = questionRepository.findBySurveyId(surveyId);
        questionRepository.deleteAll(questions);

        surveyRepository.delete(survey);
    }

    // Helper Method
    private void checkOrganizerPermission(Integer eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

        if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
            throw new AccessDeniedException("Only event organizer can perform this action");
        }
    }

    @Transactional
    public void submitSurveyAnswers(Integer eventId, SurveySubmissionRequestDto request, String userEmail) {
        MemberEvent memberEvent = memberEventRepository.findByUserEmailAndEventId(userEmail, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not registered for this event."));

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("No answers provided.");
        }

        Integer firstQId = request.getAnswers().get(0).getQuestionId();
        Question firstQ = questionRepository.findById(firstQId)
                 .orElseThrow(() -> new ResourceNotFoundException("Question not found id: " + firstQId));
        Survey survey = firstQ.getSurvey();

        if (!survey.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Survey does not belong to event id " + eventId);
        }
        
        if (responseAnswerRepository.existsByMemberEventAndQuestion_Survey(memberEvent, survey)) {
            throw new IllegalArgumentException("You have already submitted answers for this survey.");
        }

        List<Question> allQuestions = questionRepository.findBySurveyId(survey.getId());
        
        Map<Integer, SurveyAnswerRequestDto> submittedAnswersMap = request.getAnswers().stream()
                .collect(Collectors.toMap(
                    SurveyAnswerRequestDto::getQuestionId, 
                    dto -> dto,
                    (existing, replacement) -> { 
                        throw new IllegalArgumentException("Duplicate answers for question ID " + existing.getQuestionId() + " in request."); 
                    }
                ));

        List<ResponseAnswer> answersToSave = new ArrayList<>();

        for (Question question : allQuestions) {
            SurveyAnswerRequestDto ansDto = submittedAnswersMap.get(question.getId());

            // --- Validation Rule: Mandatory Fields ---
            // ถ้าไม่ใช่ Text และไม่พบคำตอบใน Map หรือคำตอบว่าง -> Error
            if (question.getQuestionType() != QuestionType.TEXT) {
                if (ansDto == null || ansDto.getAnswers() == null || ansDto.getAnswers().isEmpty()) {
                    throw new IllegalArgumentException("Question '" + question.getQuestion() + "' is mandatory.");
                }

                boolean hasContent = ansDto.getAnswers().stream().anyMatch(s -> s != null && !s.isBlank());
                if (!hasContent) {
                    throw new IllegalArgumentException("Question '" + question.getQuestion() + "' cannot be empty.");
                }
            }

            // --- Validation Rule: Single Choice limit ---
            if (ansDto != null && question.getQuestionType() == QuestionType.SINGLE) {
                if (ansDto.getAnswers().size() > 1) {
                    throw new IllegalArgumentException("Question '" + question.getQuestion() + "' accepts only one answer.");
                }
            }

            // Prepare for saving (Only if answer exists)
            if (ansDto != null && ansDto.getAnswers() != null) {
                for (String val : ansDto.getAnswers()) {
                    if (val != null && !val.isBlank()) {
                        ResponseAnswer response = new ResponseAnswer();
                        response.setMemberEvent(memberEvent);
                        response.setQuestion(question);
                        response.setQuestionType(question.getQuestionType());
                        response.setAnswer(val.trim());
                        answersToSave.add(response);
                    }
                }
            }
        }

        for (Integer submittedQId : submittedAnswersMap.keySet()) {
            boolean belongsToSurvey = allQuestions.stream().anyMatch(q -> q.getId().equals(submittedQId));
            if (!belongsToSurvey) {
                throw new IllegalArgumentException("Question ID " + submittedQId + " does not belong to the current survey.");
            }
        }

        responseAnswerRepository.saveAll(answersToSave);
    }

    // Helper Method
    private void mapChoicesToQuestion(Question q, List<String> choices) {
        q.setAnswer1(null); q.setAnswer2(null); q.setAnswer3(null); q.setAnswer4(null); q.setAnswer5(null);

        if (choices != null) {
            if (!choices.isEmpty()) q.setAnswer1(choices.get(0));
            if (choices.size() > 1) q.setAnswer2(choices.get(1));
            if (choices.size() > 2) q.setAnswer3(choices.get(2));
            if (choices.size() > 3) q.setAnswer4(choices.get(3));
            if (choices.size() > 4) q.setAnswer5(choices.get(4));
        }
    }

    private void validateQuestionCount(SurveyType type, int count) {
        if (type.name().startsWith("PRE") && count > MAX_PRE_SURVEY_QUESTIONS) {
            throw new IllegalArgumentException("Pre-survey cannot have more than " + MAX_PRE_SURVEY_QUESTIONS + " questions.");
        }
        if (type.name().startsWith("POST") && count > MAX_POST_SURVEY_QUESTIONS) {
            throw new IllegalArgumentException("Post-survey cannot have more than " + MAX_POST_SURVEY_QUESTIONS + " questions.");
        }
    }

    // Helper Method: ตรวจสอบ Choices
    private void validateQuestionContent(List<CreateQuestionRequestDto> questions) {
        for (CreateQuestionRequestDto q : questions) {
            if (q.getQuestionType() == QuestionType.SINGLE || q.getQuestionType() == QuestionType.MULTIPLE) {
                // สมมติว่า choices ส่งมาเป็น List<String> หรือ String ที่คั่นด้วย comma
                // หากใน DTO เป็น List<String> choices:
                if (q.getChoices() == null || q.getChoices().size() < MIN_CHOICES || q.getChoices().size() > MAX_CHOICES) {
                    throw new IllegalArgumentException("Question '" + q.getQuestion() + "' must have between 3 to 5 choices.");
                }
                
                // Validate: ห้ามมี Choice ซ้ำกันในข้อเดียว
                Set<String> uniqueChoices = Set.copyOf(q.getChoices());
                if (uniqueChoices.size() != q.getChoices().size()) {
                     throw new IllegalArgumentException("Question '" + q.getQuestion() + "' has duplicate choices.");
                }
            }
        }
    }
}
