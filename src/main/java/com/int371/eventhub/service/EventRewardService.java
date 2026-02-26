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
import com.int371.eventhub.entity.RewardStatus;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.EventRewardRepository;
import com.int371.eventhub.repository.ImageCategoryRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.UserRepository;
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

                                        return dto;
                                })
                                .toList();
        }
}
