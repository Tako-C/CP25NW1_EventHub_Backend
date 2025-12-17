package com.int371.eventhub.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.EditUserProfileRequestDto;
import com.int371.eventhub.dto.RegisteredEventDto;
import com.int371.eventhub.dto.UserProfileDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
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
        dto.setEventRole(registration.getEventRole().name());
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

    public EditUserProfileRequestDto editUserProfile(EditUserProfileRequestDto editRequest) {
        // ใช้ findByEmail แล้วเช็คว่ามีข้อมูลไหม
        User user = userRepository.findByEmail(editRequest.getEmail())
        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + editRequest.getEmail()));
        // ถ้าผ่านบรรทัดบนมาได้ แปลว่าเจอ User แน่นอน
        user.setFirstName(editRequest.getFirstName());
        user.setLastName(editRequest.getLastName());    
        user.setPhone(editRequest.getPhone());
        user.setAddress(editRequest.getAddress());
        user.setPostCode(editRequest.getPostCode());   
        
        // Mapping Nested Objects
        // if (editRequest.getJob() != null) {
            user.setJob(modelMapper.map(editRequest.getJob(), com.int371.eventhub.entity.Job.class));
        // }
        // if (editRequest.getCountry() != null) {
            user.setCountry(modelMapper.map(editRequest.getCountry(), com.int371.eventhub.entity.Country.class));
        // }
        // if (editRequest.getCity() != null) {
            user.setCity(modelMapper.map(editRequest.getCity(), com.int371.eventhub.entity.City.class));
        // }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, EditUserProfileRequestDto.class);
    }
}