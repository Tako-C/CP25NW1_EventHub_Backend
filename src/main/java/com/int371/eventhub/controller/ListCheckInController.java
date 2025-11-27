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
// import com.int371.eventhub.entity.MemberEvent;
// import com.int371.eventhub.entity.MemberEventRole;
// import com.int371.eventhub.entity.MemberEventRoleName;
import com.int371.eventhub.repository.EventRepository;
// import com.int371.eventhub.repository.MemberEventRepository;
// import com.int371.eventhub.repository.MemberEventRoleRepository;
import com.int371.eventhub.service.ListCheckInService;

@RestController
@RequestMapping("/list/check-in")
public class ListCheckInController {

    @Autowired
    private ListCheckInService listCheckInService;
    
    @Autowired
    private EventRepository eventRepository;

    // @Autowired
    // private MemberEventRepository memberEventRepository;

    // @Autowired
    // private MemberEventRoleRepository memberEventRoleRepository;

    @PostMapping
public ResponseEntity<ApiResponse<List<ListCheckInResponseDto>>> getListCheckIn(
        @RequestBody ListCheckInRequestDto requestDto) {

    Integer userId = listCheckInService.getUserIdFromToken();
    Integer eventId = requestDto.getEventId();

    // ตรวจ event
    Event event = eventRepository.findById(eventId).orElse(null);
    if (event == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Event not found", null));
    }

    // ตรวจว่า user เป็น creator ของ event
    if (!event.getCreatedBy().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(403, "You are not the event organizer", null));
    }

    // ตรวจ role = ORGANIZER
    // MemberEventRole role = memberEventRoleRepository.findByName(MemberEventRoleName.ORGANIZER).orElse(null);
    // if (role == null) {
    //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //             .body(new ApiResponse<>(403, "Role not found", null));
    // }

    // ตรวจว่า user มี role ใน event
    // MemberEvent userRole = memberEventRepository
    //         .findByEventRole(role)
    //         .stream()
    //         .filter(me -> me.getUser().getId().equals(userId) && me.getEvent().getId().equals(eventId))
    //         .findFirst()
    //         .orElse(null);

    // if (userRole == null) {
    //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //             .body(new ApiResponse<>(403, "Access denied", null));
    // }

    // ดึงรายการ check-in
    List<ListCheckInResponseDto> checkIns = listCheckInService.getListCheckIn();

    return ResponseEntity.ok(new ApiResponse<>(200, "Success", checkIns));
}

}
