package com.int371.eventhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.EditEventRequestDto;
import com.int371.eventhub.dto.EventRequestDto;
import com.int371.eventhub.dto.EventResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.service.EventService;
import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/events")
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

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(@ModelAttribute EventRequestDto dto) {
        Event event = eventService.createEvent(dto);
        ApiResponse<EventResponseDto> response = new ApiResponse<>(
            HttpStatus.CREATED.value(),
            "Event created successfully",
            eventService.getEventById(event.getId())
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateEvent(@PathVariable Integer id, @ModelAttribute EditEventRequestDto dto) {
        Event updatedEvent = eventService.updateEvent(id, dto);
        ApiResponse<EventResponseDto> response = new ApiResponse<>(
            HttpStatus.OK.value(),
            "Event updated successfully",
            eventService.getEventById(updatedEvent.getId())
        );
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEvent(@PathVariable Integer id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Event deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error deleting event: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}/images")
    public ResponseEntity<ApiResponse<Object>> deleteEventImage(
            @PathVariable Integer id,
            @RequestParam String category,
            @RequestParam(required = false) Integer index) {

        eventService.deleteEventImage(id, category, index);  
        ApiResponse<Object> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Image deleted successfully",
                null
        );
    return ResponseEntity.ok(response);
    }


}