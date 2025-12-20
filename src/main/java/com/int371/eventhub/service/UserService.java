package com.int371.eventhub.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.int371.eventhub.dto.CityDto;
import com.int371.eventhub.dto.CountryDto;
import com.int371.eventhub.dto.EditUserProfileRequestDto;
import com.int371.eventhub.dto.RegisteredEventDto;
import com.int371.eventhub.dto.UserProfileDto;
import com.int371.eventhub.entity.City;
import com.int371.eventhub.entity.Country;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.CountryRepository;
import com.int371.eventhub.repository.JobRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.UserRepository;
import com.int371.eventhub.repository.CityRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberEventRepository visitorEventRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private JobRepository jobRepository;


    public List<CountryDto> getCountry() {
       List<Country> countries = countryRepository.findAll();
       return countries.stream()
               .map(country -> modelMapper.map(country, CountryDto.class))
               .toList();   
    }

    public List<CityDto> getCity(Integer countryId) {
       List<City> cities = cityRepository.findByCountryId(countryId);
       return cities.stream()
               .map(city -> modelMapper.map(city, CityDto.class))
               .toList();   
    }

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

    public EditUserProfileRequestDto editUserProfile(Integer userId, EditUserProfileRequestDto editRequest) {

        if(editRequest.getFirstName() == null || editRequest.getLastName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name and Last name cannot be null.");
        }
        if (editRequest.getJob() == null || editRequest.getCountry() == null || editRequest.getCity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job, Country, and City cannot be null.");
        } else{
            // Validate Job
            if (!jobRepository.existsById(editRequest.getJob().getId())) {
                throw new ResourceNotFoundException("Job not found with ID: " + editRequest.getJob().getId());
            }
            // Validate Country
            if (!countryRepository.existsById(editRequest.getCountry().getId())) {
                throw new ResourceNotFoundException("Country not found with ID: " + editRequest.getCountry().getId());
            }
            // Validate City
            if (!cityRepository.existsById(editRequest.getCity().getId())) {
                throw new ResourceNotFoundException("City not found with ID: " + editRequest.getCity().getId());
            }
        }

        // ใช้ findByEmail แล้วเช็คว่ามีข้อมูลไหม
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // ถ้าผ่านบรรทัดบนมาได้ แปลว่าเจอ User แน่นอน
        
        user.setFirstName(editRequest.getFirstName());
        user.setLastName(editRequest.getLastName()); 
        // user.setEmail(editRequest.getEmail());   
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