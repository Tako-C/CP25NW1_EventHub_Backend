package com.int371.eventhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.EventResponseDto;
import com.int371.eventhub.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAllEvents() {
        List<EventResponseDto> events = eventService.getAllEvents();
        ApiResponse<List<EventResponseDto>> response = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Events fetched successfully", 
            events
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> getEventById(@PathVariable Integer id) {
        EventResponseDto event = eventService.getEventById(id);
        ApiResponse<EventResponseDto> response = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Event fetched successfully", 
            event
        );
        return ResponseEntity.ok(response);
    }
}