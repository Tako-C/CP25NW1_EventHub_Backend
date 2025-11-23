package com.int371.eventhub.service;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.int371.eventhub.dto.CheckInRequestDto;
import com.int371.eventhub.dto.EventRegisterRequestDto;
import com.int371.eventhub.dto.EventRegisterResponseDto;
import com.int371.eventhub.dto.LoginOtpAndEventRegisterVerifyRequestDto;
import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpRequestDto;
import com.int371.eventhub.dto.RegisterOtpVerifyRequestDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventId;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.MemberEventRoleName;
import com.int371.eventhub.entity.MemberEventStatus;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.MemberEventRoleRepository;
import com.int371.eventhub.repository.UserRepository;
import com.int371.eventhub.util.EncryptionUtil;

@Service
public class EventRegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired 
    private MemberEventRoleRepository memberEventRoleRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired 
    private QrCodeService qrCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Value("${app.qr-code.storage-path}")
    private String qrStoragePath;

    public String requestEventOtp(Integer eventId, EventRegisterRequestDto request) {
        String email = request.getEmail();

        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        boolean userExists = userRepository.existsByEmail(email);

        if (userExists) {
            if (memberEventRepository.existsByUserEmailAndEventId(email, eventId)) {
                throw new IllegalArgumentException("This email has already registered for this event.");
            }

            otpService.generateAndSendLoginOtp(email);
            return "Login OTP has been sent to your email.";

        } else {
            String randomPassword = authService.generateRandomPassword();

            RegisterOtpRequestDto registerRequest = new RegisterOtpRequestDto();
            registerRequest.setEmail(email);
            registerRequest.setFirstName(request.getFirstName());
            registerRequest.setLastName(request.getLastName());
            registerRequest.setPassword(randomPassword);

            otpService.generateAndSendOtp(registerRequest);
            return "Registration OTP has been sent to your email.";
        }
    }

    @SuppressWarnings("null")
    @Transactional
    public EventRegisterResponseDto verifyOtpAndRegister(Integer eventId, LoginOtpAndEventRegisterVerifyRequestDto request) {
        
        String email = request.getEmail();
        String otp = request.getOtp();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        Cache loginOtpCache = cacheManager.getCache("loginOtp");
        Cache registrationOtpCache = cacheManager.getCache("registrationOtp");

        if (loginOtpCache.get(email) != null) {
            otpService.verifyLoginOtp(email, otp); 

            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

            String token = jwtService.generateToken(user);
            
            if (memberEventRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
                return EventRegisterResponseDto.builder()
                        .message("You are already registered for this event. Logged in successfully.")
                        .token(token)
                        .build();
            }

            String qrCodeUrl = registerUserForEvent(user, event);
            
            return EventRegisterResponseDto.builder()
                    .message("Login and event registration successful.")
                    .token(token)
                    .qrCodeUrl(qrCodeUrl)
                    .build();
        }

        OtpData storedRegisterData = registrationOtpCache.get(email, OtpData.class);
        if (storedRegisterData != null) {
            RegisterOtpVerifyRequestDto authRequest = new RegisterOtpVerifyRequestDto();

            authRequest.setEmail(email);
            authRequest.setOtp(otp);
            authRequest.setPassword(storedRegisterData.getPassword());

            User registeredUser = authService.registerWithOtp(authRequest);
            String qrCodeUrl = registerUserForEvent(registeredUser, event);
            String token = jwtService.generateToken(registeredUser);
            
            return EventRegisterResponseDto.builder()
                    .message("New user registered and event registration successful.")
                    .token(token)
                    .qrCodeUrl(qrCodeUrl)
                    .build();
        }
        throw new IllegalArgumentException("Verification failed. No OTP was requested for this email or the OTP has expired.");
    }

    @SuppressWarnings("UseSpecificCatch")
    private String registerUserForEvent(User user, Event event) {
        try {
            MemberEventRole visitorRole = memberEventRoleRepository.findByName(MemberEventRoleName.VISITOR)
                .orElseThrow(() -> new RuntimeException("Default 'VISITOR' role not found in database."));
            MemberEvent registration = new MemberEvent(user, event, visitorRole);

            LocalDateTime now = LocalDateTime.now();
            registration.setRegisteredAt(now);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm");
            String formattedDate = registration.getRegisteredAt().format(formatter);

            String qrRawData = "UID" + user.getId() + " EID" + event.getId() + " " + formattedDate;

            String encryptedContent = encryptionUtil.encrypt(qrRawData);

            BufferedImage qrImage = qrCodeService.generateQrCodeImage(encryptedContent, 250, 250);

            String fileName = "user_" + user.getId() + "_event_" + event.getId() + ".png";
            Path storageDirectory = Paths.get(qrStoragePath);
            Path destinationFile = storageDirectory.resolve(fileName);
            
            ImageIO.write(qrImage, "png", destinationFile.toFile());

            String urlPath = "/upload/qr/" + fileName;
            registration.setImgPathQr(urlPath);
            memberEventRepository.save(registration);

            return urlPath;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate or save QR code: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String registerAuthenticatedUser(Integer eventId, String userEmail) {
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found (from token)"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        boolean alreadyRegistered = memberEventRepository.existsByUserIdAndEventId(user.getId(), event.getId());

        if (alreadyRegistered) {
            throw new IllegalArgumentException("You have already registered for this event.");
        }

        return registerUserForEvent(user, event);
    }

    @Transactional
    public String checkInUser(CheckInRequestDto request) {
        try {
            String decryptedString = encryptionUtil.decrypt(request.getQrContent());

            String[] parts = decryptedString.split(" ");
            
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid QR Code format.");
            }

            String userIdPart = parts[0];
            String eventIdPart = parts[1];

            if (!userIdPart.startsWith("UID") || !eventIdPart.startsWith("EID")) {
                throw new IllegalArgumentException("Invalid QR Code prefixes.");
            }

            Integer userId = Integer.parseInt(userIdPart.replace("UID", ""));
            Integer eventId = Integer.parseInt(eventIdPart.replace("EID", ""));

            MemberEventId id = new MemberEventId(userId, eventId);
            MemberEvent memberEvent = memberEventRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Registration data not found."));

            Event event = memberEvent.getEvent();
            if (event.getEndDate() != null && LocalDateTime.now().isAfter(event.getEndDate())) {
                throw new IllegalArgumentException("Check-in failed: The event has ended.");
            }

            if (memberEvent.getStatus() == MemberEventStatus.check_in) {
                throw new IllegalArgumentException("User already checked in.");
            }

            memberEvent.setStatus(MemberEventStatus.check_in);
            memberEventRepository.save(memberEvent);

            return "Check-in successful for user: " + memberEvent.getUser().getFirstName();

        } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Invalid ID format inside QR.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid QR Code data.");
        }
    }
}