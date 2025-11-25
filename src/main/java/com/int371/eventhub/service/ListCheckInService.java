package com.int371.eventhub.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.ListCheckInResponseDto;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.repository.MemberEventRepository;
@Service
public class ListCheckInService {

    @Autowired
    private MemberEventRepository memberRepository;

    @Autowired
    private UserService userService;

    public List<ListCheckInResponseDto> getListCheckIn() {

        List<MemberEvent> listCheckIn = memberRepository.findAll();

        return listCheckIn.stream().map(me -> {
            ListCheckInResponseDto dto = new ListCheckInResponseDto();

            dto.setName(userService.getFullName(me.getUser().getId()));
            dto.setEmail(me.getUser().getEmail());
            dto.setPhone(me.getUser().getPhone());
            dto.setRegistration_date(me.getRegisteredAt());
            dto.setStatus(me.getStatus().name());

            return dto;
        }).toList();
    }
}
