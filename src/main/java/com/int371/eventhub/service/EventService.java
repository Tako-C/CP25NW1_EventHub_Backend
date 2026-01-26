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

    @Autowired private EventRepository eventRepository;

    @Autowired private EventTypeRepository eventTypeRepository;

    @Autowired private ImageCategoryRepository categoryRepository;

    @Autowired private SurveyRepository surveyRepository;

    @Autowired private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberEventRepository memberEventRepository;


    private static final Set<String> CATEGORIES_FOR_ALL_EVENTS = Set.of("card", "slideshow");
    private static final Set<String> CATEGORIES_FOR_EVENT_BY_ID = Set.of("card", "slideshow", "detail", "map");

    // ========================= GET =========================

    // public List<EventResponseDto> getAllEvents() {
    //     List<Event> events = eventRepository.findAllByStatusNot(EventStatus.DELETED);
        
    //     if (events.isEmpty()) {
    //         throw new ResourceNotFoundException("No active events available."); 
    //     }
        
    //     return events.stream()
    //             .map(e -> convertEventToDtoWithImageStructure(e, CATEGORIES_FOR_ALL_EVENTS))
    //             .toList();
    // }

    public List<EventResponseDto> getAllEvents() {
        List<Event> events = eventRepository.findAllByStatusNot(EventStatus.DELETED);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("No active events available.");
        }
        return events.stream()
                .map(event -> {
                    EventResponseDto dto =
                            convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_ALL_EVENTS);

                    boolean hasActivePreSurvey = hasActiveSurveyByTypes(
                            event.getId(),
                            List.of(
                                    SurveyType.PRE_VISITOR,
                                    SurveyType.PRE_EXHIBITOR
                            )
                    );
                    boolean hasActivePostSurvey = hasActiveSurveyByTypes(
                            event.getId(),
                            List.of(
                                    SurveyType.POST_VISITOR,
                                    SurveyType.POST_EXHIBITOR
                            )
                    );
                    dto.setHasPreSurvey(hasActivePreSurvey);
                    dto.setHasPostSurvey(hasActivePostSurvey);
                    return dto;
                })
                .toList();
    }


   public EventResponseDto getEventById(Integer id) {
        Event event = eventRepository.findByIdAndStatusNot(id, EventStatus.DELETED)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found with id: " + id)
                );
        boolean hasActivePreSurvey = hasActiveSurveyByTypes(
                id,
                List.of(
                        SurveyType.PRE_VISITOR,
                        SurveyType.PRE_EXHIBITOR
                )
        );
        boolean hasActivePostSurvey = hasActiveSurveyByTypes(
                id,
                List.of(
                        SurveyType.POST_VISITOR,
                        SurveyType.POST_EXHIBITOR
                )
        );

        EventResponseDto result =
                convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);

        result.setHasPreSurvey(hasActivePreSurvey);
        result.setHasPostSurvey(hasActivePostSurvey);

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

        if (event.getImages() == null) event.setImages(new ArrayList<>());

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
                        processImage(file, "slideshow", eventId, event, i + 1)
                    );
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
            memberEvent.setId(memberEventId);

            memberEvent.setUser(organizer);
            memberEvent.setEvent(savedEvent);

            memberEvent.setEventRole(MemberEventRole.ORGANIZER);
            memberEvent.setStatus(MemberEventStatus.REGISTRATION); 

            memberEventRepository.save(memberEvent);

        return modelMapper.map(savedEvent, EventResponseDto.class);
    }

    // ========================= UPDATE =========================

    @Transactional
    public Event updateEvent(Integer id, EditEventRequestDto dto) {
        for (int index = 0; index < dto.getSlideshowIndices().size(); index++) {
            Integer targetIndex = dto.getSlideshowIndices().get(index);
            if (targetIndex < 1 || targetIndex > 3) {
                throw new IllegalArgumentException("Slideshow index must be between 1 and 3. Invalid index: " + targetIndex);
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
                        processImage(file, "slideshow", eventId, event, index)
                    );
                }
            }
        }

        return eventRepository.save(event);
    }

    // ========================= DELETE =========================
    @Transactional
    public void deleteEvent(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // if (event.getImages() != null) {
        //     for (EventImage img : event.getImages()) {
        //         deleteImageFile(img.getImgPathEv());
        //     }
        // }
        // eventRepository.delete(event);
        event.setStatus(EventStatus.DELETED);
        eventRepository.save(event);
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
            Integer index
    ) {
        try {
            String fileName = generateFileName(eventId, category, index, file.getOriginalFilename());

            Path dir = Paths.get(uploadBaseDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
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
        } catch (IOException ignored) {}
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

    // private EventResponseDto convertEventToDtoWithImageStructure(Event event, Set<String> allowed) {
    //     EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);

    //     if (event.getImages() != null) {
    //         Map<String, String> map = event.getImages().stream()
    //             .filter(i -> allowed.contains(i.getCategory().getCategoryName().toLowerCase()))
    //             .collect(Collectors.toMap(
    //                 i -> i.getCategory().getCategoryName().toLowerCase(),
    //                 EventImage::getImgPathEv,
    //                 (a, b) -> a
    //             ));

    //         EventImageResponseDto img = new EventImageResponseDto();
    //         img.setImageCard(map.get("card"));
    //         img.setImageSlideShow(map.get("slideshow"));
    //         img.setImageDetail(map.get("detail"));
    //         img.setImageMap(map.get("map"));
    //         dto.setImages(img);
    //     }
    //     return dto;
    // }

    // ========================= Status Event By Survey =========================
        private boolean hasActiveSurveyByTypes(Integer eventId,List<SurveyType> surveyTypes) {
        return surveyRepository
                .findByEventIdAndStatusAndTypeIn(
                        eventId,
                        SurveyStatus.ACTIVE,
                        surveyTypes
                )
                .isEmpty() == false;
    }

}
