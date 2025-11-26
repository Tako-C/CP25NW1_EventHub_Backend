package com.int371.eventhub.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.RegisteredEventDto;
import com.int371.eventhub.dto.UserProfileDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberEventRepository visitorEventRepository;

    @Autowired
    private ModelMapper modelMapper;
    public String getFullName(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        return user.getFirstName() + " " + user.getLastName();
    }

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return modelMapper.map(user, UserProfileDto.class);
    }

    public List<RegisteredEventDto> getRegisteredEvents(String email) {
        List<MemberEvent> registrations = visitorEventRepository.findByUserEmail(email);

        return registrations.stream()
                .map(this::mapToRegisteredEventDto)
                .toList();
    }

    private RegisteredEventDto mapToRegisteredEventDto(MemberEvent registration) {
        Event event = registration.getEvent();
        
        RegisteredEventDto dto = new RegisteredEventDto();
        
        dto.setEventId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setLocation(event.getLocation());
        dto.setStatus(registration.getStatus().name());
        dto.setRegisteredAt(registration.getRegisteredAt());
        dto.setEventRole(registration.getEventRole().getName().name());
        dto.setQrCodeUrl(registration.getImgPathQr());

        if (event.getImages() != null) {
            String cardImage = event.getImages().stream()
                .filter(img -> img.getCategory() != null && "card".equalsIgnoreCase(img.getCategory().getCategoryName()))
                .map(EventImage::getImgPathEv)
                .findFirst()
                .orElse(null);
            dto.setImageCard(cardImage);
        }

        return dto;
    }
}