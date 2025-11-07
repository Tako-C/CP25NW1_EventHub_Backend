package com.int371.eventhub.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.EventImageResponse;
import com.int371.eventhub.dto.EventResponse;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final Set<String> CATEGORIES_FOR_ALL_EVENTS = Set.of("card", "slideshow");
    private static final Set<String> CATEGORIES_FOR_EVENT_BY_ID = Set.of("detail", "map");

public List<EventResponse> getAllEvents() {
    List<Event> events = eventRepository.findAll();

    if (events.isEmpty()) {
        throw new ResourceNotFoundException("No events available.");
    }
    
    return events.stream()
                .map(event -> convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_ALL_EVENTS))
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        return convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);
    
    }

    private EventResponse convertEventToDtoWithImageStructure(Event event, Set<String> allowedCategoryNames) {
        EventResponse dto = modelMapper.map(event, EventResponse.class);

        if (event.getImages() != null && !event.getImages().isEmpty()) {
            Map<String, String> imageMap = event.getImages().stream()
                .filter(img -> img.getCategory() != null && img.getCategory().getCategoryName() != null)
                .map(img -> {
                    img.getCategory().setCategoryName(img.getCategory().getCategoryName().toLowerCase());
                    return img;
                })
                .filter(img -> allowedCategoryNames.contains(img.getCategory().getCategoryName()))
                .collect(Collectors.toMap(
                    img -> img.getCategory().getCategoryName(),
                    EventImage::getImgPathEv,
                    (existingPath, newPath) -> existingPath 
                ));
            EventImageResponse imageResponse = new EventImageResponse();
            imageResponse.setImageCard(imageMap.get("card"));
            imageResponse.setImageSlideShow(imageMap.get("slideshow"));
            imageResponse.setImageDetail(imageMap.get("detail"));
            imageResponse.setImageMap(imageMap.get("map"));
            
            dto.setImages(imageResponse);
        }

        return dto;
    }
}