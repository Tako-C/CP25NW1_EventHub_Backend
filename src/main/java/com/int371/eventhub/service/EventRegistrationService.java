package com.int371.eventhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.int371.eventhub.dto.EventRegisterRequestDto;
import com.int371.eventhub.dto.EventRegisterResponseDto;
import com.int371.eventhub.dto.EventRegisterVerifyDto;
import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpRequest;
import com.int371.eventhub.dto.RegisterOtpVerificationRequest;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.entity.VisitorEvent;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.UserRepository;
import com.int371.eventhub.repository.VisitorEventRepository;

@Service
public class EventRegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VisitorEventRepository visitorEventRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CacheManager cacheManager;

    public String requestEventOtp(Integer eventId, EventRegisterRequestDto request) {
        String email = request.getEmail();

        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        boolean userExists = userRepository.existsByEmail(email);

        if (userExists) {
            if (visitorEventRepository.existsByUserEmailAndEventId(email, eventId)) {
                throw new IllegalArgumentException("This email has already registered for this event.");
            }

            otpService.generateAndSendLoginOtp(email);
            return "Login OTP has been sent to your email.";

        } else {
            String randomPassword = authService.generateRandomPassword();

            RegisterOtpRequest registerRequest = new RegisterOtpRequest();
            registerRequest.setEmail(email);
            registerRequest.setFirstName(request.getFirstName());
            registerRequest.setLastName(request.getLastName());
            registerRequest.setPassword(randomPassword);

            otpService.generateAndSendOtp(registerRequest);
            return "Registration OTP has been sent to your email.";
        }
    }

    @Transactional
    public EventRegisterResponseDto verifyOtpAndRegister(Integer eventId, EventRegisterVerifyDto request) {
        
        String email = request.getEmail();
        String otp = request.getOtp();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        Cache loginOtpCache = cacheManager.getCache("loginOtp");
        Cache registrationOtpCache = cacheManager.getCache("registrationOtp");

        if (loginOtpCache.get(email) != null) {
            otpService.verifyLoginOtp(email, otp); 

            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.")); // ควรจะต้องเจอ

            String token = jwtService.generateToken(user);
            
            if (visitorEventRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
                return new EventRegisterResponseDto("You are already registered for this event. Logged in successfully.", token);
            }

            registerUserForEvent(user, event);
            return new EventRegisterResponseDto("Login and event registration successful.", token);
        }

        OtpData storedRegisterData = registrationOtpCache.get(email, OtpData.class);
        if (storedRegisterData != null) {
            RegisterOtpVerificationRequest authRequest = new RegisterOtpVerificationRequest();

            authRequest.setEmail(email);
            authRequest.setOtp(otp);
            authRequest.setPassword(storedRegisterData.getPassword());

            User registeredUser = authService.registerWithOtp(authRequest);
            registerUserForEvent(registeredUser, event);
            String token = jwtService.generateToken(registeredUser);
            
            return new EventRegisterResponseDto("New user registered and event registration successful.", token);
        }
        throw new IllegalArgumentException("Verification failed. No OTP was requested for this email or the OTP has expired.");
    }

    private void registerUserForEvent(User user, Event event) {
        VisitorEvent registration = new VisitorEvent(user, event);
        visitorEventRepository.save(registration);
    }

    @Transactional
    public void registerAuthenticatedUser(Integer eventId, String userEmail) {
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found (from token)"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        boolean alreadyRegistered = visitorEventRepository.existsByUserIdAndEventId(user.getId(), event.getId());

        if (alreadyRegistered) {
            throw new IllegalArgumentException("You have already registered for this event.");
        }

        registerUserForEvent(user, event);
    }
}