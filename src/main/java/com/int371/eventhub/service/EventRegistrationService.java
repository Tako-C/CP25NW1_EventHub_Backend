package com.int371.eventhub.service;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.int371.eventhub.dto.CheckInPreviewResponseDto;
import com.int371.eventhub.dto.CheckInRequestDto;
import com.int371.eventhub.dto.CheckInResponseDto;
import com.int371.eventhub.dto.EventRegisterRequestDto;
import com.int371.eventhub.dto.EventRegisterResponseDto;
import com.int371.eventhub.dto.GenericResponse;
import com.int371.eventhub.dto.LoginOtpAndEventRegisterVerifyRequestDto;
import com.int371.eventhub.dto.ManualCheckInRequestDto;
import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpRequestDto;
import com.int371.eventhub.dto.RegisterOtpVerifyRequestDto;
import com.int371.eventhub.dto.SearchUserCheckInRequestDto;
import com.int371.eventhub.dto.SearchUserCheckInResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventStatus;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.MemberEventStatus;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
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
    private EncryptionUtil encryptionUtil;

    @Value("${app.qr-code.storage-path}")
    private String qrStoragePath;

    public String requestEventOtp(Integer eventId, EventRegisterRequestDto request) {
        String email = request.getEmail();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getStatus() == EventStatus.DELETED) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        boolean userExists = userRepository.existsByEmailIgnoreCase(email);

        if (userExists) {
            // ... (check memberEventRepository, etc.)
            if (memberEventRepository.existsByUserEmailAndEventId(email, eventId)) {
                throw new IllegalArgumentException("This email has already registered for this event.");
            }

            // ... (rest of logic)
            otpService.generateAndSendLoginOtp(email);
            return "Login OTP has been sent to your email.";

        } else {
            // ... (registration logic)
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
    public EventRegisterResponseDto verifyOtpAndRegister(Integer eventId,
            LoginOtpAndEventRegisterVerifyRequestDto request) {

        String email = request.getEmail();
        String otp = request.getOtp();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getStatus() == EventStatus.DELETED) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        Cache loginOtpCache = cacheManager.getCache("loginOtp");
        Cache registrationOtpCache = cacheManager.getCache("registrationOtp");

        // Check Login OTP Cache
        if (loginOtpCache.get(email) != null) {
            otpService.verifyLoginOtp(email, otp);

            User user = userRepository.findByEmailIgnoreCase(email)
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

            User registeredUser = authService.registerWithOtp(authRequest, true);
            String qrCodeUrl = registerUserForEvent(registeredUser, event);
            String token = jwtService.generateToken(registeredUser);

            return EventRegisterResponseDto.builder()
                    .message("New user registered and event registration successful.")
                    .token(token)
                    .qrCodeUrl(qrCodeUrl)
                    .build();
        }
        throw new IllegalArgumentException(
                "Verification failed. No OTP was requested for this email or the OTP has expired.");
    }

    @SuppressWarnings("UseSpecificCatch")
    private String registerUserForEvent(User user, Event event) {
        try {
            MemberEventRole visitorRole = MemberEventRole.VISITOR;
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

        if (event.getStatus() == EventStatus.DELETED) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        boolean alreadyRegistered = memberEventRepository.existsByUserIdAndEventId(user.getId(), event.getId());

        if (alreadyRegistered) {
            throw new IllegalArgumentException("You have already registered for this event.");
        }

        return registerUserForEvent(user, event);
    }

    @Transactional
    public GenericResponse<CheckInResponseDto> checkInUser(CheckInRequestDto request, String requesterEmail) {
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

            validateCheckInPermissions(requesterEmail, eventId);

            MemberEvent memberEvent = memberEventRepository.findByUserIdAndEventId(userId, eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Registration data not found."));

            Event event = memberEvent.getEvent();
            if (event.getEndDate() != null && LocalDateTime.now().isAfter(event.getEndDate())) {
                throw new IllegalArgumentException("Check-in failed: The event has ended.");
            }

            if (memberEvent.getStatus() == MemberEventStatus.CHECK_IN) {
                throw new IllegalArgumentException("User already checked in.");
            }

            memberEvent.setStatus(MemberEventStatus.CHECK_IN);
            memberEvent.setUpdatedAt(LocalDateTime.now());
            memberEventRepository.save(memberEvent);

            CheckInResponseDto responseDto = new CheckInResponseDto();
            responseDto.setUserId(memberEvent.getUser().getId().toString());
            responseDto.setUserName(memberEvent.getUser().getFirstName() + " " + memberEvent.getUser().getLastName());
            responseDto.setEmail(memberEvent.getUser().getEmail());
            responseDto.setPhone(memberEvent.getUser().getPhone());
            responseDto.setEventName(memberEvent.getEvent().getEventName());
            return new GenericResponse<>("Check-in successful", responseDto);

        } catch (AccessDeniedException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format inside QR.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid QR Code data.");
        }
    }

    @Transactional
    public String manualCheckInUser(ManualCheckInRequestDto request, String requesterEmail) {
        try {
            validateCheckInPermissions(requesterEmail, request.getEventId());

            MemberEvent memberEvent = memberEventRepository
                    .findByUserIdAndEventId(request.getUserId(), request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Registration data not found."));

            Event event = memberEvent.getEvent();
            if (event.getEndDate() != null && LocalDateTime.now().isAfter(event.getEndDate())) {
                throw new IllegalArgumentException("Check-in failed: The event has ended.");
            }

            if (memberEvent.getStatus() == MemberEventStatus.CHECK_IN) {
                throw new IllegalArgumentException("User already checked in.");
            }

            memberEvent.setStatus(MemberEventStatus.CHECK_IN);
            memberEventRepository.save(memberEvent);

            return "Check-in successful for user: " + memberEvent.getUser().getFirstName();
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Manual check-in failed: " + e.getMessage());
        }

    }

    public List<SearchUserCheckInResponseDto> searchUser(SearchUserCheckInRequestDto request) {
        String query = request.getQuery().trim();
        Integer eventId = request.getEventId();

        System.err.println("Searching for query: " + query + " in event ID: " + eventId);

        // ค้นหาข้อมูลจาก Repository (ครอบคลุมทั้ง Exact Email/Phone และ Partial Name)
        List<MemberEvent> members = memberEventRepository.searchVisitorsFlexibly(eventId, query);

        if (members.isEmpty()) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลผู้ลงทะเบียนที่ตรงกับ: " + query);
        }

        // แปลงจาก Entity เป็น DTO List
        return members.stream().map(me -> {
            User user = me.getUser();
            SearchUserCheckInResponseDto dto = new SearchUserCheckInResponseDto();
            dto.setUserId(user.getId());
            dto.setName(user.getFirstName() + " " + user.getLastName());
            dto.setEmail(user.getEmail());
            // แนะนำให้ส่ง Phone ไปด้วยเพื่อให้ Admin แยกแยะคนชื่อซ้ำได้ง่ายขึ้น
            dto.setStatus(me.getStatus().toString());
            return dto;
        }).collect(Collectors.toList());
    }

    public CheckInPreviewResponseDto getCheckInPreview(CheckInRequestDto request, String requesterEmail) {
        try {
            String decryptedString = encryptionUtil.decrypt(request.getQrContent());
            String[] parts = decryptedString.split(" ");

            if (parts.length < 3)
                throw new IllegalArgumentException("Invalid QR Code format.");

            String userIdPart = parts[0];
            String eventIdPart = parts[1];

            if (!userIdPart.startsWith("UID") || !eventIdPart.startsWith("EID")) {
                throw new IllegalArgumentException("Invalid QR Code prefixes.");
            }

            Integer userId = Integer.parseInt(userIdPart.replace("UID", ""));
            Integer eventId = Integer.parseInt(eventIdPart.replace("EID", ""));

            validateCheckInPermissions(requesterEmail, eventId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found from QR data."));

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found from QR data."));

            CheckInPreviewResponseDto response = new CheckInPreviewResponseDto();

            CheckInPreviewResponseDto.UserPreviewDto userDto = new CheckInPreviewResponseDto.UserPreviewDto();
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setImgPath(user.getImgPath());
            response.setUserProfile(userDto);

            CheckInPreviewResponseDto.EventPreviewDto eventDto = new CheckInPreviewResponseDto.EventPreviewDto();
            eventDto.setEventName(event.getEventName());
            response.setEventDetail(eventDto);

            return response;

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("QR Decryption Error: " + e.getMessage());
            throw new IllegalArgumentException("Invalid QR Code data.");
        }
    }

    private void validateCheckInPermissions(String requesterEmail, Integer eventId) {
        MemberEvent requester = memberEventRepository.findByUserEmailAndEventId(requesterEmail, eventId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this event."));

        if (requester.getEventRole() != MemberEventRole.ORGANIZER
                && requester.getEventRole() != MemberEventRole.STAFF) {
            throw new AccessDeniedException("Only Organizer or Staff can perform check-in operations.");
        }
    }
}