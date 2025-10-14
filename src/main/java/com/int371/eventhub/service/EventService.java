package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.EventResponse;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ModelMapper modelMapper;

public List<EventResponse> getAllEvents() {
    List<Event> events = eventRepository.findAll();

    if (events.isEmpty()) {
        throw new ResourceNotFoundException("No events available.");
    }
    
    return events.stream()
            .map(event -> modelMapper.map(event, EventResponse.class))
            .collect(Collectors.toList());
    }

    public EventResponse getEventById(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        return modelMapper.map(event, EventResponse.class);
    }
}