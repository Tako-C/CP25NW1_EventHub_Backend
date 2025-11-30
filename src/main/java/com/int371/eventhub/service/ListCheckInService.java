package com.int371.eventhub.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.int371.eventhub.dto.ListCheckInResponseDto;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRoleName;
import com.int371.eventhub.repository.MemberEventRepository;

import jakarta.servlet.http.HttpServletRequest;
@Service
public class ListCheckInService {

    @Autowired
    private MemberEventRepository memberRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private HttpServletRequest request;

    public List<ListCheckInResponseDto> getListCheckIn(Integer eventId) {
        MemberEventRoleName roleEvent = MemberEventRoleName.VISITOR;
        List<MemberEvent> listCheckIn = memberRepository.findByEventIdAndEventRoleName(eventId, roleEvent);
        return listCheckIn.stream().map(me -> {
            ListCheckInResponseDto dto = new ListCheckInResponseDto();

            dto.setName(userService.getFullName(me.getUser().getId()));
            dto.setEmail(me.getUser().getEmail());
            dto.setPhone(me.getUser().getPhone());
            dto.setRegistration_date(me.getRegisteredAt());
            dto.setCheck_in_at(me.getUpdatedAt());
            dto.setStatus(me.getStatus().name());

            return dto;
        }).toList();
    }

    
    public Integer getUserIdFromToken() {
        String authorizationHeader = request.getHeader("Authorization");

        String jwtToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
        }

        if (jwtToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Bearer Token is missing or improperly formatted.");
        }
        
        Integer checkJwtUserId = jwtService.extractUserId(jwtToken);
        if (checkJwtUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token claims.");
        }
        return checkJwtUserId;
    }
}
