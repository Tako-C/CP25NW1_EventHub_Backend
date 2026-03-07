package com.int371.eventhub.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.int371.eventhub.dto.EditEventRequestDto;
import com.int371.eventhub.dto.EventImageResponseDto;
import com.int371.eventhub.dto.EventRequestDto;
import com.int371.eventhub.dto.EventResponseDto;
import com.int371.eventhub.dto.EventTypeDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.entity.EventStatus;
import com.int371.eventhub.entity.EventType;
import com.int371.eventhub.entity.ImageCategory;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventId;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.MemberEventStatus;
import com.int371.eventhub.entity.SurveyStatus;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.EventTypeRepository;
import com.int371.eventhub.repository.ImageCategoryRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.SurveyRepository;
import com.int371.eventhub.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {

    @Value("${app.event-image.storage-path}")
    private String uploadBaseDir;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private ImageCategoryRepository categoryRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired
    private com.int371.eventhub.repository.ResponseAnswerRepository responseAnswerRepository;

    @Autowired
    private com.int371.eventhub.repository.EventRewardRepository eventRewardRepository;

    @Autowired
    private com.int371.eventhub.repository.UserRewardRepository userRewardRepository;

    @Autowired
    private com.int371.eventhub.repository.QuestionRepository questionRepository;

    private static final Set<String> CATEGORIES_FOR_ALL_EVENTS = Set.of("card", "slideshow");
    private static final Set<String> CATEGORIES_FOR_EVENT_BY_ID = Set.of("card", "slideshow", "detail", "map");

    // ========================= GET =========================

    // public List<EventResponseDto> getAllEvents() {
    // List<Event> events = eventRepository.findAllByStatusNot(EventStatus.DELETED);

    // if (events.isEmpty()) {
    // throw new ResourceNotFoundException("No active events available.");
    // }

    // return events.stream()
    // .map(e -> convertEventToDtoWithImageStructure(e, CATEGORIES_FOR_ALL_EVENTS))
    // .toList();
    // }

    public List<EventResponseDto> getAllEvents() {
        List<Event> events = eventRepository.findAllByStatusNot(EventStatus.DELETED);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("No active events available.");
        }
        return events.stream()
                .map(event -> {
                    EventResponseDto dto = convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_ALL_EVENTS);

                    boolean hasActivePreSurvey = hasActiveSurveyByTypes(
                            event.getId(),
                            List.of(
                                    SurveyType.PRE_VISITOR,
                                    SurveyType.PRE_EXHIBITOR));
                    boolean hasActivePostSurvey = hasActiveSurveyByTypes(
                            event.getId(),
                            List.of(
                                    SurveyType.POST_VISITOR,
                                    SurveyType.POST_EXHIBITOR));
                    dto.setHasPreSurvey(hasActivePreSurvey);
                    dto.setHasPostSurvey(hasActivePostSurvey);
                    dto.setEventStatus(event.getStatus());
                    return dto;
                })
                .toList();
    }

    public List<EventResponseDto> getAllEventsForAdmin() {
        List<Event> events = eventRepository.findAll();
        // if (events.isEmpty()) {
        // throw new ResourceNotFoundException("No events available.");
        // }
        return events.stream()
                .map(event -> {
                    EventResponseDto dto = convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_ALL_EVENTS);

                    boolean hasActivePreSurvey = hasActiveSurveyByTypes(
                            event.getId(),
                            List.of(
                                    SurveyType.PRE_VISITOR,
                                    SurveyType.PRE_EXHIBITOR));
                    boolean hasActivePostSurvey = hasActiveSurveyByTypes(
                            event.getId(),
                            List.of(
                                    SurveyType.POST_VISITOR,
                                    SurveyType.POST_EXHIBITOR));
                    dto.setHasPreSurvey(hasActivePreSurvey);
                    dto.setHasPostSurvey(hasActivePostSurvey);
                    dto.setEventStatus(event.getStatus());
                    return dto;
                })
                .toList();
    }

    public EventResponseDto getEventByIdForAdmin(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        boolean hasActivePreSurvey = hasActiveSurveyByTypes(
                id,
                List.of(
                        SurveyType.PRE_VISITOR,
                        SurveyType.PRE_EXHIBITOR));
        boolean hasActivePostSurvey = hasActiveSurveyByTypes(
                id,
                List.of(
                        SurveyType.POST_VISITOR,
                        SurveyType.POST_EXHIBITOR));

        EventResponseDto result = convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);

        result.setHasPreSurvey(hasActivePreSurvey);
        result.setHasPostSurvey(hasActivePostSurvey);
        result.setEventStatus(event.getStatus());

        return result;
    }

    public EventResponseDto getEventById(Integer id, String email) {
        Event event = eventRepository.findByIdAndStatusNot(id, EventStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        boolean hasActivePreSurvey = hasActiveSurveyByTypes(
                id,
                List.of(
                        SurveyType.PRE_VISITOR,
                        SurveyType.PRE_EXHIBITOR));
        boolean hasActivePostSurvey = hasActiveSurveyByTypes(
                id,
                List.of(
                        SurveyType.POST_VISITOR,
                        SurveyType.POST_EXHIBITOR));

        EventResponseDto result = convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);

        result.setHasPreSurvey(hasActivePreSurvey);
        result.setHasPostSurvey(hasActivePostSurvey);

        // Check if user completed post survey
        boolean isPreSurveyCompleted = false;
        boolean isPostSurveyCompleted = false;
        if (email != null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user).orElse(null);
                if (memberEvent != null) {
                    result.setCheckInStatus(memberEvent.getStatus().name());

                    if (memberEvent.getDonePreSurvey() != null && memberEvent.getDonePreSurvey() == 1) {
                        isPreSurveyCompleted = true;
                    }

                    SurveyType targetType = null;
                    if (memberEvent.getEventRole() == MemberEventRole.VISITOR) {
                        targetType = SurveyType.POST_VISITOR;
                    } else if (memberEvent.getEventRole() == MemberEventRole.EXHIBITOR) {
                        targetType = SurveyType.POST_EXHIBITOR;
                    }

                    if (targetType != null) {
                        java.util.Optional<com.int371.eventhub.entity.Survey> surveyOpt = surveyRepository
                                .findByEventIdAndStatusAndType(id, SurveyStatus.ACTIVE, targetType);
                        if (surveyOpt.isPresent()) {
                            isPostSurveyCompleted = responseAnswerRepository
                                    .existsByMemberEventAndQuestion_Survey(memberEvent, surveyOpt.get());
                        }
                    }
                }
            }
        }
        result.setPreSurveyCompleted(isPreSurveyCompleted);
        result.setPostSurveyCompleted(isPostSurveyCompleted);
        result.setEventStatus(event.getStatus());

        return result;
    }

    // ========================= CREATE =========================

    @Transactional
    public EventResponseDto createEvent(EventRequestDto dto, String email) {
        // VALIDATE slideshow (ห้ามเกิน 3)
        if (dto.getEventSlideshow() != null && dto.getEventSlideshow().size() > 3) {
            throw new IllegalArgumentException("Event slideshow can contain at most 3 images");
        }

        Event event = modelMapper.map(dto, Event.class);
        event.setId(null);

        EventType type = eventTypeRepository.findById(dto.getEventTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EventType not found"));
        event.setEventTypeId(type);

        if (event.getImages() == null)
            event.setImages(new ArrayList<>());

        // 👉 save รอบแรก เพื่อให้ได้ eventId
        event = eventRepository.save(event);
        Integer eventId = event.getId();

        if (isNotBlank(dto.getEventCard()))
            event.getImages().add(processImage(dto.getEventCard(), "card", eventId, event, null));

        if (isNotBlank(dto.getEventDetail()))
            event.getImages().add(processImage(dto.getEventDetail(), "detail", eventId, event, null));

        if (isNotBlank(dto.getEventMap()))
            event.getImages().add(processImage(dto.getEventMap(), "map", eventId, event, null));

        if (dto.getEventSlideshow() != null) {
            for (int i = 0; i < dto.getEventSlideshow().size(); i++) {
                MultipartFile file = dto.getEventSlideshow().get(i);
                if (isNotBlank(file)) {
                    event.getImages().add(
                            processImage(file, "slideshow", eventId, event, i + 1));
                }
            }
        }

        Event savedEvent = eventRepository.save(event);

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        MemberEvent memberEvent = new MemberEvent();

        MemberEventId memberEventId = new MemberEventId();
        memberEventId.setUserId(organizer.getId());
        memberEventId.setEventId(savedEvent.getId());
        // memberEvent.setId(memberEventId);

        memberEvent.setUser(organizer);
        memberEvent.setEvent(savedEvent);

        memberEvent.setEventRole(MemberEventRole.ORGANIZER);
        memberEvent.setStatus(MemberEventStatus.REGISTRATION);

        memberEventRepository.save(memberEvent);

        EventResponseDto responseDto = modelMapper.map(savedEvent, EventResponseDto.class);
        responseDto.setEventStatus(savedEvent.getStatus());
        return responseDto;
    }

    @Transactional
    public EventResponseDto createEventForAdmin(EventRequestDto dto, String adminEmail) {
        if (dto.getEventSlideshow() != null && dto.getEventSlideshow().size() > 3) {
            throw new IllegalArgumentException("Event slideshow can contain at most 3 images");
        }

        Event event = modelMapper.map(dto, Event.class);
        event.setId(null);

        EventType type = eventTypeRepository.findById(dto.getEventTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EventType not found"));
        event.setEventTypeId(type);

        if (event.getImages() == null)
            event.setImages(new ArrayList<>());

        event = eventRepository.save(event);
        Integer eventId = event.getId();

        if (isNotBlank(dto.getEventCard()))
            event.getImages().add(processImage(dto.getEventCard(), "card", eventId, event, null));

        if (isNotBlank(dto.getEventDetail()))
            event.getImages().add(processImage(dto.getEventDetail(), "detail", eventId, event, null));

        if (isNotBlank(dto.getEventMap()))
            event.getImages().add(processImage(dto.getEventMap(), "map", eventId, event, null));

        if (dto.getEventSlideshow() != null) {
            for (int i = 0; i < dto.getEventSlideshow().size(); i++) {
                MultipartFile file = dto.getEventSlideshow().get(i);
                if (isNotBlank(file)) {
                    event.getImages().add(
                            processImage(file, "slideshow", eventId, event, i + 1));
                }
            }
        }

        Event savedEvent = eventRepository.save(event);

        // Admin becomes the organizer
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found: " + adminEmail));

        MemberEvent memberEvent = new MemberEvent();
        memberEvent.setUser(admin);
        memberEvent.setEvent(savedEvent);
        memberEvent.setEventRole(MemberEventRole.ORGANIZER);
        memberEvent.setStatus(MemberEventStatus.REGISTRATION);
        memberEventRepository.save(memberEvent);

        EventResponseDto responseDto = modelMapper.map(savedEvent, EventResponseDto.class);
        responseDto.setEventStatus(savedEvent.getStatus());
        return responseDto;
    }

    // ========================= UPDATE =========================

    @Transactional
    public Event updateEvent(Integer id, EditEventRequestDto dto) {
        for (int index = 0; index < dto.getSlideshowIndices().size(); index++) {
            Integer targetIndex = dto.getSlideshowIndices().get(index);
            if (targetIndex < 1 || targetIndex > 3) {
                throw new IllegalArgumentException(
                        "Slideshow index must be between 1 and 3. Invalid index: " + targetIndex);
            }
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.DELETED) {
            throw new ResourceNotFoundException("Event not found or has been deleted");
        }

        Integer eventId = event.getId();

        event.setEventName(dto.getEventName());
        event.setEventDesc(dto.getEventDesc());
        event.setLocation(dto.getLocation());
        event.setHostOrganisation(dto.getHostOrganisation());
        event.setContactEmail(dto.getContactEmail());
        event.setContactFacebook(dto.getContactFacebook());
        event.setContactLine(dto.getContactLine());
        event.setContactPhone(dto.getContactPhone());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setCreatedBy(dto.getCreatedBy());
        event.setUpdatedAt(LocalDateTime.now());

        EventStatus targetStatus = dto.getStatus() != null ? dto.getStatus() : event.getStatus();
        validateEventStatusTimeline(targetStatus, event.getStartDate(), event.getEndDate());
        event.setStatus(targetStatus);
        // Update Event Type if provided

        if (dto.getEventTypeId() != null) {
            EventType type = eventTypeRepository.findById(dto.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventType not found"));
            event.setEventTypeId(type);
        }

        if (isNotBlank(dto.getEventCard()))
            updateSingleImage(dto.getEventCard(), "card", eventId, event);

        if (isNotBlank(dto.getEventDetail()))
            updateSingleImage(dto.getEventDetail(), "detail", eventId, event);

        if (isNotBlank(dto.getEventMap()))
            updateSingleImage(dto.getEventMap(), "map", eventId, event);

        if (dto.getEventSlideshow() != null && dto.getSlideshowIndices() != null) {
            int size = Math.min(dto.getEventSlideshow().size(), dto.getSlideshowIndices().size());
            for (int i = 0; i < size; i++) {
                MultipartFile file = dto.getEventSlideshow().get(i);
                Integer index = dto.getSlideshowIndices().get(i);
                if (isNotBlank(file)) {
                    removeImageByCategoryAndIndex(event, "slideshow", index);
                    event.getImages().add(
                            processImage(file, "slideshow", eventId, event, index));
                }
            }
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEventForAdmin(Integer id, EditEventRequestDto dto) {
        if (dto.getSlideshowIndices() != null) {
            for (int index = 0; index < dto.getSlideshowIndices().size(); index++) {
                Integer targetIndex = dto.getSlideshowIndices().get(index);
                if (targetIndex < 1 || targetIndex > 3) {
                    throw new IllegalArgumentException(
                            "Slideshow index must be between 1 and 3. Invalid index: " + targetIndex);
                }
            }
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        Integer eventId = event.getId();

        if (dto.getEventName() != null)
            event.setEventName(dto.getEventName());
        if (dto.getEventDesc() != null)
            event.setEventDesc(dto.getEventDesc());
        if (dto.getLocation() != null)
            event.setLocation(dto.getLocation());
        if (dto.getHostOrganisation() != null)
            event.setHostOrganisation(dto.getHostOrganisation());
        if (dto.getContactEmail() != null)
            event.setContactEmail(dto.getContactEmail());
        if (dto.getContactFacebook() != null)
            event.setContactFacebook(dto.getContactFacebook());
        if (dto.getContactLine() != null)
            event.setContactLine(dto.getContactLine());
        if (dto.getContactPhone() != null)
            event.setContactPhone(dto.getContactPhone());
        if (dto.getStartDate() != null)
            event.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null)
            event.setEndDate(dto.getEndDate());
        if (dto.getCreatedBy() != null)
            event.setCreatedBy(dto.getCreatedBy());
        event.setUpdatedAt(LocalDateTime.now());

        EventStatus targetStatusAdmin = dto.getStatus() != null ? dto.getStatus() : event.getStatus();
        validateEventStatusTimeline(targetStatusAdmin, event.getStartDate(), event.getEndDate());
        event.setStatus(targetStatusAdmin);

        if (dto.getEventTypeId() != null) {
            EventType type = eventTypeRepository.findById(dto.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventType not found"));
            event.setEventTypeId(type);
        }

        if (isNotBlank(dto.getEventCard()))
            updateSingleImage(dto.getEventCard(), "card", eventId, event);

        if (isNotBlank(dto.getEventDetail()))
            updateSingleImage(dto.getEventDetail(), "detail", eventId, event);

        if (isNotBlank(dto.getEventMap()))
            updateSingleImage(dto.getEventMap(), "map", eventId, event);

        if (dto.getEventSlideshow() != null && dto.getSlideshowIndices() != null) {
            int size = Math.min(dto.getEventSlideshow().size(), dto.getSlideshowIndices().size());
            for (int i = 0; i < size; i++) {
                MultipartFile file = dto.getEventSlideshow().get(i);
                Integer index = dto.getSlideshowIndices().get(i);
                if (isNotBlank(file)) {
                    removeImageByCategoryAndIndex(event, "slideshow", index);
                    event.getImages().add(
                            processImage(file, "slideshow", eventId, event, index));
                }
            }
        }

        return eventRepository.save(event);
    }

    private void validateEventStatusTimeline(EventStatus targetStatus, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (targetStatus == null || targetStatus == EventStatus.DELETED) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (startDate != null && now.isBefore(startDate)) {
            if (targetStatus != EventStatus.UPCOMING) {
                throw new IllegalArgumentException(
                        "Cannot change status to " + targetStatus
                                + ". The current time is before the start date, so the status must be UPCOMING.");
            }
        } else if (endDate != null && now.isAfter(endDate)) {
            if (targetStatus != EventStatus.FINISHED) {
                throw new IllegalArgumentException(
                        "Cannot change status to " + targetStatus
                                + ". The event has already ended, so the status must be FINISHED. Please edit the dates if you want to set it to "
                                + targetStatus + ".");
            }
        } else if (startDate != null && endDate != null && !now.isBefore(startDate) && !now.isAfter(endDate)) {
            if (targetStatus != EventStatus.ONGOING) {
                throw new IllegalArgumentException(
                        "Cannot change status to " + targetStatus
                                + ". The event is currently running, so the status must be ONGOING.");
            }
        }
    }

    // ========================= DELETE =========================
    @Transactional
    public void deleteEvent(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // if (event.getImages() != null) {
        // for (EventImage img : event.getImages()) {
        // deleteImageFile(img.getImgPathEv());
        event.setStatus(EventStatus.DELETED);
        eventRepository.save(event);
    }

    @Transactional
    public void hardDeleteEventForAdmin(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // 1. Fetch & Delete ResponseAnswers
        List<MemberEvent> members = memberEventRepository.findByEventId(eventId);
        if (!members.isEmpty()) {
            List<com.int371.eventhub.entity.ResponseAnswer> answers = responseAnswerRepository
                    .findByMemberEventIn(members);
            if (!answers.isEmpty()) {
                responseAnswerRepository.deleteAll(answers);
            }
        }

        // 2. Fetch & Delete Questions, then Surveys
        List<com.int371.eventhub.entity.Survey> surveys = surveyRepository.findByEventId(eventId);
        if (!surveys.isEmpty()) {
            List<com.int371.eventhub.entity.Question> questions = questionRepository.findBySurveyIn(surveys);
            if (!questions.isEmpty()) {
                questionRepository.deleteAll(questions);
            }
            surveyRepository.deleteAll(surveys);
        }

        // 3. Delete UserRewards, then EventRewards
        List<com.int371.eventhub.entity.EventReward> rewards = eventRewardRepository.findByEventId(eventId);
        if (!rewards.isEmpty()) {
            List<com.int371.eventhub.entity.UserReward> userRewards = userRewardRepository.findByEventRewardIn(rewards);
            if (!userRewards.isEmpty()) {
                userRewardRepository.deleteAll(userRewards);
            }

            // Cleanup Reward Images from Disk before removing from DB
            for (com.int371.eventhub.entity.EventReward reward : rewards) {
                String expectedPattern = "_reward_" + reward.getId() + ".";
                if (event.getImages() != null) {
                    List<EventImage> imagesToRemove = event.getImages().stream()
                            .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("reward"))
                            .filter(img -> img.getImgPathEv().contains(expectedPattern))
                            .toList();

                    for (EventImage img : imagesToRemove) {
                        deleteImageFile(img.getImgPathEv());
                        event.getImages().remove(img);
                    }
                }
            }
            eventRewardRepository.deleteAll(rewards);
        }

        // 4. Delete MemberEvents
        if (!members.isEmpty()) {
            memberEventRepository.deleteAll(members);
        }

        // 5. Delete Event Images (from disk, as cascade will handle DB)
        if (event.getImages() != null) {
            for (EventImage img : new ArrayList<>(event.getImages())) {
                deleteImageFile(img.getImgPathEv());
            }
            event.getImages().clear();
        }

        // 6. Finally, Delete the Event itself
        eventRepository.delete(event);
    }

    @Transactional
    public void deleteEventImage(Integer eventId, String category, Integer index) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        category = category.toLowerCase();

        if ("slideshow".equals(category)) {
            if (index == null) {
                throw new IllegalArgumentException("Slideshow image requires index");
            }
            deleteSlideshowImage(event, index);
        } else {
            deleteSingleImage(event, category);
        }
    }

    // ========================= IMAGE CORE =========================

    private EventImage processImage(
            MultipartFile file,
            String category,
            Integer eventId,
            Event event,
            Integer index) {
        try {
            String fileName = generateFileName(eventId, category, index, file.getOriginalFilename());

            Path dir = Paths.get(uploadBaseDir);
            if (!Files.exists(dir))
                Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            EventImage img = new EventImage();
            img.setUploadedAt(LocalDateTime.now());
            img.setImgPathEv(fileName);
            img.setEvent(event);

            ImageCategory cat = categoryRepository.findByCategoryName(category)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + category));
            img.setCategory(cat);

            return img;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    private String generateFileName(Integer eventId, String category, Integer index, String originalName) {
        String ext = originalName.substring(originalName.lastIndexOf("."));
        if (index != null)
            return String.format("%d_%s_%d%s", eventId, category, index, ext);
        return String.format("%d_%s%s", eventId, category, ext);
    }

    private void updateSingleImage(MultipartFile file, String category, Integer eventId, Event event) {
        removeImageByCategoryAndIndex(event, category, null);
        event.getImages().add(processImage(file, category, eventId, event, null));
    }

    private void removeImageByCategoryAndIndex(Event event, String category, Integer index) {
        String pattern = (index != null) ? "_" + category + "_" + index + "." : "_" + category + ".";
        List<EventImage> toRemove = event.getImages().stream()
                .filter(i -> i.getImgPathEv().contains(pattern))
                .toList();

        for (EventImage img : toRemove) {
            deleteImageFile(img.getImgPathEv());
            event.getImages().remove(img);
        }
    }

    private void deleteImageFile(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(uploadBaseDir).resolve(fileName));
        } catch (IOException ignored) {
        }
    }

    private boolean isNotBlank(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void deleteSingleImage(Event event, String category) {
        boolean removed = event.getImages().removeIf(img -> {
            boolean match = img.getCategory()
                    .getCategoryName()
                    .equalsIgnoreCase(category);

            if (match) {
                deleteImageFile(img.getImgPathEv());
            }
            return match;
        });
        if (!removed) {
            throw new ResourceNotFoundException("Image not found: " + category);
        }
    }

    private void deleteSlideshowImage(Event event, int index) {
        String pattern = "_slideshow_" + index + ".";
        EventImage target = event.getImages().stream()
                .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("slideshow"))
                .filter(img -> img.getImgPathEv().contains(pattern))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Slideshow image not found at index " + index));

        deleteImageFile(target.getImgPathEv());
        event.getImages().remove(target);
    }

    // ========================= DTO MAP =========================

    private EventResponseDto convertEventToDtoWithImageStructure(
            Event event,
            Set<String> allowedCategoryNames) {

        EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);

        if (event.getImages() == null || event.getImages().isEmpty()) {
            return dto;
        }

        EventImageResponseDto imageDto = new EventImageResponseDto();

        // CARD
        event.getImages().stream()
                .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("Card"))
                .findFirst()
                .ifPresent(img -> imageDto.setImageCard(img.getImgPathEv()));

        // DETAIL
        event.getImages().stream()
                .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("Detail"))
                .findFirst()
                .ifPresent(img -> imageDto.setImageDetail(img.getImgPathEv()));

        // MAP
        event.getImages().stream()
                .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("map"))
                .findFirst()
                .ifPresent(img -> imageDto.setImageMap(img.getImgPathEv()));

        // SLIDESHOW (LIST)
        List<String> slideshow = event.getImages().stream()
                .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("slideshow"))
                .sorted((a, b) -> a.getImgPathEv().compareTo(b.getImgPathEv())) // เรียง 1,2,3
                .map(EventImage::getImgPathEv)
                .toList();

        imageDto.setImageSlideShow(slideshow);

        dto.setImages(imageDto);
        return dto;
    }

    public List<EventTypeDto> getAllEventTypes() {
        List<EventType> eventTypes = eventTypeRepository.findAll();

        return eventTypes.stream()
                .map(type -> modelMapper.map(type, EventTypeDto.class))
                .toList();
    }

    public List<String> getAllEventImageTypes() {
        List<ImageCategory> categories = categoryRepository.findAll();

        return categories.stream()
                .map(ImageCategory::getCategoryName)
                .toList();
    }

    // private EventResponseDto convertEventToDtoWithImageStructure(Event event,
    // Set<String> allowed) {
    // EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);

    // if (event.getImages() != null) {
    // Map<String, String> map = event.getImages().stream()
    // .filter(i ->
    // allowed.contains(i.getCategory().getCategoryName().toLowerCase()))
    // .collect(Collectors.toMap(
    // i -> i.getCategory().getCategoryName().toLowerCase(),
    // EventImage::getImgPathEv,
    // (a, b) -> a
    // ));

    // EventImageResponseDto img = new EventImageResponseDto();
    // img.setImageCard(map.get("card"));
    // img.setImageSlideShow(map.get("slideshow"));
    // img.setImageDetail(map.get("detail"));
    // img.setImageMap(map.get("map"));
    // dto.setImages(img);
    // }
    // return dto;
    // }

    // ========================= Status Event By Survey =========================
    private boolean hasActiveSurveyByTypes(Integer eventId, List<SurveyType> surveyTypes) {
        return surveyRepository
                .findByEventIdAndStatusAndTypeIn(
                        eventId,
                        SurveyStatus.ACTIVE,
                        surveyTypes)
                .isEmpty() == false;
    }

}
