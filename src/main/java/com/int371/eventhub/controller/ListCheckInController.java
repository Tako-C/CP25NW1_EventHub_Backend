package com.int371.eventhub.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.int371.eventhub.dto.ApiResponse;
import com.int371.eventhub.dto.ListCheckInRequestDto;
import com.int371.eventhub.dto.ListCheckInResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.service.ListCheckInService;

@RestController
@RequestMapping("/list/check-in")
public class ListCheckInController {

    @Autowired
    private ListCheckInService listCheckInService;
    
    @Autowired
    private EventRepository eventRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<List<ListCheckInResponseDto>>> getListCheckIn(
        @RequestBody ListCheckInRequestDto requestDto) {

    Integer userId = listCheckInService.getUserIdFromToken();
    Integer eventId = requestDto.getEventId();

    Event event = eventRepository.findById(eventId).orElse(null);
    if (event == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Event not found", null));
    }

    if (!event.getCreatedBy().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(403, "You are not the event organizer", null));
    }
    List<ListCheckInResponseDto> checkIns = listCheckInService.getListCheckIn(eventId);
    return ResponseEntity.ok(new ApiResponse<>(200, "Success", checkIns));
}
}
