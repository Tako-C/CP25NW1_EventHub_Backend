package com.int371.eventhub.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.int371.eventhub.dto.CreateEventRewardRequestDto;
import com.int371.eventhub.dto.EventRewardResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventReward;
import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.MemberEventStatus;
import com.int371.eventhub.entity.RewardStatus;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.entity.UserReward;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.EventRewardRepository;
import com.int371.eventhub.repository.ImageCategoryRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.UserRepository;
import com.int371.eventhub.repository.UserRewardRepository;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.entity.ImageCategory;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class EventRewardService {

        @Autowired
        private EventRewardRepository eventRewardRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private MemberEventRepository memberEventRepository;

        @Autowired
        private ModelMapper modelMapper;

        @Autowired
        private ImageCategoryRepository categoryRepository;

        @Autowired
        private UserRewardRepository userRewardRepository;

        @Value("${app.event-image.storage-path}")
        private String uploadBaseDir;

        @Transactional
        public EventRewardResponseDto createReward(Integer eventId, CreateEventRewardRequestDto request,
                        String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found with id: " + eventId));

                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

                if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
                        throw new AccessDeniedException("Only event organizer can create rewards");
                }

                if (!event.getCreatedBy().equals(user.getId())) {
                        throw new AccessDeniedException("Only the event owner can create rewards");
                }

                EventReward reward = new EventReward();
                reward.setName(request.getName());
                reward.setDescription(request.getDescription());
                reward.setEvent(event);
                reward.setRequirementType(request.getRequirementType());
                reward.setStartRedeemAt(request.getStartRedeemAt());
                reward.setEndRedeemAt(request.getEndRedeemAt());
                reward.setQuantity(request.getQuantity());
                // Temporarily set a blank status or early properties to generate ID
                reward.setStatus(RewardStatus.ACTIVE);

                EventReward savedReward = eventRewardRepository.save(reward);

                // Handle Image upload if present
                if (request.getImage() != null && !request.getImage().isEmpty()) {
                        String contentType = request.getImage().getContentType();
                        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png")
                                        || contentType.equals("image/jpg"))) {
                                throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
                        }

                        try {
                                String originalName = request.getImage().getOriginalFilename();
                                String ext = originalName != null
                                                ? originalName.substring(originalName.lastIndexOf("."))
                                                : ".jpg";
                                String fileName = String.format("%d_reward_%d%s", eventId, savedReward.getId(), ext);

                                java.nio.file.Path dir = Paths.get(uploadBaseDir);
                                if (!Files.exists(dir)) {
                                        Files.createDirectories(dir);
                                }

                                Files.copy(request.getImage().getInputStream(), dir.resolve(fileName),
                                                StandardCopyOption.REPLACE_EXISTING);

                                EventImage img = new EventImage();
                                img.setUploadedAt(LocalDateTime.now());
                                img.setImgPathEv(fileName);
                                img.setEvent(event);

                                ImageCategory cat = categoryRepository.findByCategoryName("reward")
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Category 'reward' not found"));
                                img.setCategory(cat);

                                if (event.getImages() == null) {
                                        event.setImages(new ArrayList<>());
                                }
                                event.getImages().add(img);
                                eventRepository.save(event); // Cascade the image save

                        } catch (IOException e) {
                                throw new RuntimeException("Failed to save reward image", e);
                        }
                }

                EventRewardResponseDto response = modelMapper.map(savedReward, EventRewardResponseDto.class);
                response.setEventId(savedReward.getEvent().getId());

                return response;
        }

        @Transactional
        public EventRewardResponseDto updateReward(Integer eventId, Integer rewardId,
                        CreateEventRewardRequestDto request,
                        String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found with id: " + eventId));

                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

                if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
                        throw new AccessDeniedException("Only event organizer can update rewards");
                }

                if (!event.getCreatedBy().equals(user.getId())) {
                        throw new AccessDeniedException("Only the event owner can update rewards");
                }

                EventReward reward = eventRewardRepository.findById(rewardId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

                if (!reward.getEvent().getId().equals(eventId)) {
                        throw new IllegalArgumentException("Reward does not belong to this event");
                }

                if (request.getName() != null && !request.getName().trim().isEmpty()) {
                        reward.setName(request.getName());
                }
                if (request.getDescription() != null) {
                        reward.setDescription(request.getDescription());
                }
                if (request.getRequirementType() != null) {
                        reward.setRequirementType(request.getRequirementType());
                }
                if (request.getStartRedeemAt() != null) {
                        reward.setStartRedeemAt(request.getStartRedeemAt());
                }
                if (request.getEndRedeemAt() != null) {
                        reward.setEndRedeemAt(request.getEndRedeemAt());
                }
                if (request.getQuantity() != null) {
                        reward.setQuantity(request.getQuantity());
                }

                EventReward savedReward = eventRewardRepository.save(reward);

                if (request.getImage() != null && !request.getImage().isEmpty()) {
                        String contentType = request.getImage().getContentType();
                        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png")
                                        || contentType.equals("image/jpg"))) {
                                throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
                        }

                        try {
                                String originalName = request.getImage().getOriginalFilename();
                                String ext = originalName != null
                                                ? originalName.substring(originalName.lastIndexOf("."))
                                                : ".jpg";
                                String fileName = String.format("%d_reward_%d%s", eventId, savedReward.getId(), ext);

                                java.nio.file.Path dir = Paths.get(uploadBaseDir);
                                if (!Files.exists(dir)) {
                                        Files.createDirectories(dir);
                                }

                                String expectedPattern = "_reward_" + reward.getId() + ".";
                                EventImage existingImage = null;
                                if (event.getImages() != null) {
                                        existingImage = event.getImages().stream()
                                                        .filter(img -> img.getCategory().getCategoryName()
                                                                        .equalsIgnoreCase("reward"))
                                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                                        .findFirst().orElse(null);
                                }

                                if (existingImage != null) {
                                        try {
                                                Files.deleteIfExists(dir.resolve(existingImage.getImgPathEv()));
                                        } catch (Exception e) {
                                        }
                                        existingImage.setImgPathEv(fileName);
                                        existingImage.setUploadedAt(LocalDateTime.now());
                                } else {
                                        EventImage img = new EventImage();
                                        img.setUploadedAt(LocalDateTime.now());
                                        img.setImgPathEv(fileName);
                                        img.setEvent(event);

                                        ImageCategory cat = categoryRepository.findByCategoryName("reward")
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Category 'reward' not found"));
                                        img.setCategory(cat);

                                        if (event.getImages() == null) {
                                                event.setImages(new ArrayList<>());
                                        }
                                        event.getImages().add(img);
                                }

                                Files.copy(request.getImage().getInputStream(), dir.resolve(fileName),
                                                StandardCopyOption.REPLACE_EXISTING);
                                eventRepository.save(event);

                        } catch (IOException e) {
                                throw new RuntimeException("Failed to update reward image", e);
                        }
                }

                EventRewardResponseDto response = modelMapper.map(savedReward, EventRewardResponseDto.class);
                response.setEventId(savedReward.getEvent().getId());

                String expectedPattern = "_reward_" + savedReward.getId() + ".";
                if (savedReward.getEvent().getImages() != null) {
                        savedReward.getEvent().getImages().stream()
                                        .filter(img -> img.getCategory().getCategoryName()
                                                        .equalsIgnoreCase("reward"))
                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                        .findFirst()
                                        .ifPresent(img -> response.setImagePath(img.getImgPathEv()));
                }

                return response;
        }

        @Transactional
        public void deleteReward(Integer eventId, Integer rewardId, String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found with id: " + eventId));

                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

                if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
                        throw new AccessDeniedException("Only event organizer can delete rewards");
                }

                if (!event.getCreatedBy().equals(user.getId())) {
                        throw new AccessDeniedException("Only the event owner can delete rewards");
                }

                EventReward reward = eventRewardRepository.findById(rewardId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

                if (!reward.getEvent().getId().equals(eventId)) {
                        throw new IllegalArgumentException("Reward does not belong to this event");
                }

                String expectedPattern = "_reward_" + reward.getId() + ".";
                if (event.getImages() != null) {
                        EventImage existingImage = event.getImages().stream()
                                        .filter(img -> img.getCategory().getCategoryName()
                                                        .equalsIgnoreCase("reward"))
                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                        .findFirst().orElse(null);

                        if (existingImage != null) {
                                java.nio.file.Path dir = Paths.get(uploadBaseDir);
                                try {
                                        Files.deleteIfExists(dir.resolve(existingImage.getImgPathEv()));
                                } catch (Exception e) {
                                }
                                event.getImages().remove(existingImage);
                                eventRepository.save(event);
                        }
                }

                eventRewardRepository.delete(reward);
        }

        @Transactional(readOnly = true)
        public List<EventRewardResponseDto> getRewardsForOrganizer(Integer eventId, String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found with id: " + eventId));

                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

                if (memberEvent.getEventRole() != MemberEventRole.ORGANIZER) {
                        throw new AccessDeniedException("Only event organizer can view all rewards details");
                }

                List<EventReward> rewards = eventRewardRepository.findByEventId(eventId);
                return rewards.stream()
                                .map(reward -> {
                                        EventRewardResponseDto dto = modelMapper.map(reward,
                                                        EventRewardResponseDto.class);
                                        dto.setEventId(reward.getEvent().getId());

                                        // ดึงภาพของรางวัลจากตาราง IMAGES (EventImage) ผ่าน Event
                                        String expectedPattern = "_reward_" + reward.getId() + ".";
                                        reward.getEvent().getImages().stream()
                                                        .filter(img -> img.getCategory().getCategoryName()
                                                                        .equalsIgnoreCase("reward"))
                                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                                        .findFirst()
                                                        .ifPresent(img -> dto.setImagePath(img.getImgPathEv()));

                                        return dto;
                                })
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<EventRewardResponseDto> getRewardsForVisitor(Integer eventId, String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found with id: " + eventId));

                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                                .orElseThrow(() -> new AccessDeniedException("User is not a member of this event"));

                // allow VISITOR and EXHIBITOR to see rewards
                if (memberEvent.getEventRole() != MemberEventRole.VISITOR
                                && memberEvent.getEventRole() != MemberEventRole.EXHIBITOR) {
                        throw new AccessDeniedException("Role not permitted to view rewards as a participant");
                }

                List<EventReward> rewards = eventRewardRepository.findByEventIdAndStatusIn(
                                eventId,
                                List.of(RewardStatus.ACTIVE, RewardStatus.OUT_OF_STOCK));

                return rewards.stream()
                                .map(reward -> {
                                        EventRewardResponseDto dto = modelMapper.map(reward,
                                                        EventRewardResponseDto.class);
                                        dto.setEventId(reward.getEvent().getId());

                                        // ดึงภาพของรางวัลจากตาราง IMAGES (EventImage) ผ่าน Event
                                        String expectedPattern = "_reward_" + reward.getId() + ".";
                                        reward.getEvent().getImages().stream()
                                                        .filter(img -> img.getCategory().getCategoryName()
                                                                        .equalsIgnoreCase("reward"))
                                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                                        .findFirst()
                                                        .ifPresent(img -> dto.setImagePath(img.getImgPathEv()));

                                        // ตรวจสอบสิทธิ์ตาม requirementType
                                        boolean eligible = switch (reward.getRequirementType()) {
                                                case FREE -> true;
                                                case CHECKED_IN ->
                                                        memberEvent.getStatus() == MemberEventStatus.CHECK_IN;
                                                case PRE_SURVEY_DONE -> memberEvent.getDonePreSurvey() != null
                                                                && memberEvent.getDonePreSurvey() == 1;
                                                case POST_SURVEY_DONE -> memberEvent.getDonePostSurvey() != null
                                                                && memberEvent.getDonePostSurvey() == 1;
                                        };
                                        dto.setEligible(eligible);

                                        return dto;
                                })
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<EventRewardResponseDto> getAllRewards() {
                List<EventReward> rewards = eventRewardRepository.findAll();
                return rewards.stream()
                                .map(reward -> {
                                        EventRewardResponseDto dto = modelMapper.map(reward,
                                                        EventRewardResponseDto.class);
                                        dto.setEventId(reward.getEvent().getId());

                                        // ดึงภาพของรางวัลจากตาราง IMAGES (EventImage) ผ่าน Event
                                        String expectedPattern = "_reward_" + reward.getId() + ".";
                                        reward.getEvent().getImages().stream()
                                                        .filter(img -> img.getCategory().getCategoryName()
                                                                        .equalsIgnoreCase("reward"))
                                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                                        .findFirst()
                                                        .ifPresent(img -> dto.setImagePath(img.getImgPathEv()));

                                        return dto;
                                })
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<EventRewardResponseDto> getRewardsByUserId(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ที่มี ID: " + userId));

                List<UserReward> userRewards = userRewardRepository.findByUserId(user.getId());
                return userRewards.stream()
                                .map(ur -> {
                                        EventReward reward = ur.getEventReward();
                                        EventRewardResponseDto dto = modelMapper.map(reward,
                                                        EventRewardResponseDto.class);
                                        dto.setEventId(reward.getEvent().getId());

                                        // ดึงภาพของรางวัลจากตาราง IMAGES (EventImage) ผ่าน Event
                                        String expectedPattern = "_reward_" + reward.getId() + ".";
                                        reward.getEvent().getImages().stream()
                                                        .filter(img -> img.getCategory().getCategoryName()
                                                                        .equalsIgnoreCase("reward"))
                                                        .filter(img -> img.getImgPathEv().contains(expectedPattern))
                                                        .findFirst()
                                                        .ifPresent(img -> dto.setImagePath(img.getImgPathEv()));

                                        return dto;
                                })
                                .toList();
        }

        @Transactional
        public String redeemReward(com.int371.eventhub.dto.RedeemRewardRequest request) {
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with id: " + request.getUserId()));

                EventReward reward = eventRewardRepository.findById(request.getEventRewardId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Reward not found with id: " + request.getEventRewardId()));

                // ตรวจสอบว่า User ลงทะเบียนเข้างานนี้หรือไม่
                Event event = reward.getEvent();
                MemberEvent memberEvent = memberEventRepository.findByEventAndUser(event, user)
                                .orElseThrow(() -> new AccessDeniedException(
                                                "User is not registered for this event."));

                // ตรวจสอบว่าเป็น VISITOR หรือไม่
                if (memberEvent.getEventRole() != MemberEventRole.VISITOR) {
                        throw new IllegalArgumentException("Only users with the VISITOR role can redeem rewards.");
                }

                // ตรวจสอบเงื่อนไขตาม RewardRequirementType
                switch (reward.getRequirementType()) {
                        case FREE:
                                // แค่ลงทะเบียนเข้างานก็สามารถแลกได้
                                break;
                        case CHECKED_IN:
                                if (memberEvent.getStatus() != MemberEventStatus.CHECK_IN) {
                                        throw new IllegalArgumentException("User has not checked in to the event.");
                                }
                                break;
                        case PRE_SURVEY_DONE:
                                if (memberEvent.getDonePreSurvey() == null || memberEvent.getDonePreSurvey() != 1) {
                                        throw new IllegalArgumentException("User has not completed the pre-survey.");
                                }
                                break;
                        case POST_SURVEY_DONE:
                                if (memberEvent.getDonePostSurvey() == null || memberEvent.getDonePostSurvey() != 1) {
                                        throw new IllegalArgumentException("User has not completed the post-survey.");
                                }
                                break;
                        default:
                                throw new IllegalArgumentException("Unknown reward requirement type.");
                }

                if (reward.getStatus() != RewardStatus.ACTIVE) {
                        throw new IllegalArgumentException("This reward is not currently active.");
                }

                if (reward.getQuantity() != null && reward.getQuantity() <= 0) {
                        throw new IllegalArgumentException("This reward is out of stock.");
                }

                LocalDateTime now = LocalDateTime.now();
                if (reward.getStartRedeemAt() != null && now.isBefore(reward.getStartRedeemAt())) {
                        throw new IllegalArgumentException("Reward redemption has not started yet.");
                }
                if (reward.getEndRedeemAt() != null && now.isAfter(reward.getEndRedeemAt())) {
                        throw new IllegalArgumentException("Reward redemption period has ended.");
                }

                if (userRewardRepository.existsByUserAndEventReward(user, reward)) {
                        throw new IllegalArgumentException("User has already redeemed this reward.");
                }

                if (reward.getQuantity() != null) {
                        reward.setQuantity(reward.getQuantity() - 1);
                        if (reward.getQuantity() == 0) {
                                reward.setStatus(RewardStatus.OUT_OF_STOCK);
                        }
                        eventRewardRepository.save(reward);
                }

                com.int371.eventhub.entity.UserReward userReward = new com.int371.eventhub.entity.UserReward();
                userReward.setUser(user);
                userReward.setEventReward(reward);
                userRewardRepository.save(userReward);

                return "Redeemed successfully";
        }
}
