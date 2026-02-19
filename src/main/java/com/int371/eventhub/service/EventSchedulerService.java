package com.int371.eventhub.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventStatus;
import com.int371.eventhub.repository.EventRepository;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.Survey;
import com.int371.eventhub.entity.SurveyStatus;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.SurveyRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventSchedulerService {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MemberEventRepository memberEventRepository;
    @Autowired
    private SurveyRepository surveyRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtService jwtService;

    @Scheduled(fixedRate = 60000)
    // ลบ @Transactional ออกจากที่นี่ เพื่อไม่ให้ Lock DB นานเกินไปตอนส่งเมล
    public void updateEventStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Current Server Time: {}", now);

        // ส่วนที่ 1 & 2: อัปเดตสถานะ (แยกเป็นอีก method ที่มี @Transactional)
        processStatusUpdates(now);
    }

    @Transactional
    public void processStatusUpdates(LocalDateTime now) {
        // 1. UPCOMING -> ONGOING
        List<Event> upcomingToOngoing = eventRepository.findAllByStartDateBeforeAndStatus(now, EventStatus.UPCOMING);
        if (!upcomingToOngoing.isEmpty()) {
            upcomingToOngoing.forEach(e -> e.setStatus(EventStatus.ONGOING));
            eventRepository.saveAll(upcomingToOngoing);
        }

        // 2. ONGOING / UPCOMING -> FINISHED
        List<EventStatus> activeStatuses = List.of(EventStatus.UPCOMING, EventStatus.ONGOING);
        List<Event> expiredEvents = eventRepository.findAllByEndDateBeforeAndStatusIn(now, activeStatuses);

        if (!expiredEvents.isEmpty()) {
            for (Event event : expiredEvents) {
                event.setStatus(EventStatus.FINISHED);
            }
            eventRepository.saveAll(expiredEvents);

            // ส่งเมล (อยู่นอก Transaction หลักของสถานะอีเวนต์จะดีกว่า หรือเรียกแยก)
            for (Event event : expiredEvents) {
                sendPostSurveyEmails(event);
            }
        }
    }

    // private void sendPostSurveyEmails(Event event) {
    // try {
    // List<MemberEvent> members =
    // memberEventRepository.findByEventId(event.getId());
    // List<Survey> surveys = surveyRepository.findByEventIdAndStatus(event.getId(),
    // SurveyStatus.ACTIVE);

    // // ตรวจสอบว่ามี Post-Survey ไหม
    // boolean hasVisitorSurvey = surveys.stream().anyMatch(s -> s.getType() ==
    // SurveyType.POST_VISITOR);
    // boolean hasExhibitorSurvey = surveys.stream().anyMatch(s -> s.getType() ==
    // SurveyType.POST_EXHIBITOR);

    // if (!hasVisitorSurvey && !hasExhibitorSurvey) return;

    // for (MemberEvent member : members) {
    // if (shouldSendEmail(member, hasVisitorSurvey, hasExhibitorSurvey)) {

    // Optional<SurveyToken> existingToken =
    // surveyTokenRepository.findByUserIdAndEventId(
    // member.getUser().getId(),
    // event.getId()

    // );
    // Optional<User> user = userRepository.findById(member.getUser().getId());

    // if (existingToken.isPresent()) {
    // continue;
    // }

    // createNewTokenAndSendEmail(member, event , user);
    // }
    // }
    // } catch (Exception e) {
    // log.error("Failed to send post-survey emails for event {}", event.getId(),
    // e);
    // }
    // }

    public void sendPostSurveyEmails(Event event) {
        try {
            // 1. Fetch data ที่จำเป็นทั้งหมดมาทีเดียว
            // List<MemberEvent> members =
            // memberEventRepository.findByEventId(event.getId());
            List<Survey> surveys = surveyRepository.findByEventIdAndStatus(event.getId(), SurveyStatus.ACTIVE);
            List<MemberEvent> members = memberEventRepository.findByEventIdAndSendEmail(event.getId(), 0);

            boolean hasVisitorSurvey = surveys.stream().anyMatch(s -> s.getType() == SurveyType.POST_VISITOR);
            boolean hasExhibitorSurvey = surveys.stream().anyMatch(s -> s.getType() == SurveyType.POST_EXHIBITOR);

            if (!hasVisitorSurvey && !hasExhibitorSurvey) {
                log.info("No active post-surveys for event {}", event.getId());
                return;
            }

            for (MemberEvent member : members) {
                // 2. เช็ค Role ก่อนทำ Logic หนักๆ
                if (shouldSendEmail(member, hasVisitorSurvey, hasExhibitorSurvey)) {

                    // 3. เรียกใช้ Helper Method โดยตรง (ไม่ต้องมี @Transactional แล้วถ้าไม่บันทึก
                    // DB)
                    processEmailSending(member, event);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send post-survey emails for event {}", event.getId(), e);
        }
    }

    private void processEmailSending(MemberEvent member, Event event) {
        User user = member.getUser();

        try {
            // 1️⃣ สร้าง JWT Token
            String surveyToken = jwtService.generateSurveyToken(user, member);

            // 2️⃣ ส่ง Email
            emailService.sendPostSurveyEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    event.getEventName(),
                    event.getId(),
                    surveyToken);

            // 3️⃣ ถ้าสำเร็จ → set = 1
            member.setSendEmail(1);
            log.info("Successfully sent survey email to: {}", user.getEmail());

        } catch (Exception e) {

            // ❌ ถ้าส่งไม่สำเร็จ → set = 2
            member.setSendEmail(2);
            log.error("Error sending survey email to user {}: {}",
                    user.getEmail(), e.getMessage());
        }

        memberEventRepository.save(member);
    }

    // @Transactional
    // protected void createNewTokenAndSendEmail(MemberEvent member, Event event ,
    // User user) {
    // try {
    // // SurveyToken surveyToken = new SurveyToken();
    // // surveyToken.setUser(member.getUser());
    // // surveyToken.setEvent(event);
    // // surveyToken.setExpiryDate(LocalDateTime.now().plusDays(7));
    // // surveyToken.setUsed(false);

    // // surveyToken = surveyTokenRepository.save(surveyToken);

    // String surveyToken = jwtService.generateSurveyToken(user , member);
    // emailService.sendPostSurveyEmail(
    // member.getUser().getEmail(),
    // member.getUser().getFirstName(),
    // event.getEventName(),
    // event.getId(),
    // surveyToken
    // );
    // jwtService.generateSurveyToken(user , member);
    // } catch (Exception e) {
    // log.error("Error creating token for user {}", member.getUser().getEmail(),
    // e);
    // }
    // }

    private boolean shouldSendEmail(MemberEvent member, boolean hasVisitor, boolean hasExhibitor) {
        return (member.getEventRole() == MemberEventRole.VISITOR && hasVisitor) ||
                (member.getEventRole() == MemberEventRole.EXHIBITOR && hasExhibitor);
    }

    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void retryPostSurveyEmails() {
        log.info("Starting retry mechanism for post-survey emails...");

        // 1. Fetch pending emails (Batch size 50)
        // sendEmail = 0 (New) or 2 (Failed)
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 50);
        List<MemberEvent> pendingMembers = memberEventRepository.findByEventStatusAndSendEmailIn(
                EventStatus.FINISHED,
                List.of(0, 2),
                pageable);

        if (pendingMembers.isEmpty()) {
            log.info("No pending post-survey emails found.");
            return;
        }

        log.info("Found {} pending emails. Processing...", pendingMembers.size());

        for (MemberEvent member : pendingMembers) {
            Event event = member.getEvent();

            try {
                List<Survey> surveys = surveyRepository.findByEventIdAndStatus(event.getId(), SurveyStatus.ACTIVE);
                boolean hasVisitorSurvey = surveys.stream().anyMatch(s -> s.getType() == SurveyType.POST_VISITOR);
                boolean hasExhibitorSurvey = surveys.stream().anyMatch(s -> s.getType() == SurveyType.POST_EXHIBITOR);

                if (!hasVisitorSurvey && !hasExhibitorSurvey) {
                    continue;
                }

                if (shouldSendEmail(member, hasVisitorSurvey, hasExhibitorSurvey)) {
                    processEmailSending(member, event);

                    // Delay to avoid spam
                    try {
                        Thread.sleep(2000); // 2 seconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    member.setSendEmail(3); // 3 = Not applicable / Skipped
                    memberEventRepository.save(member);
                }

            } catch (Exception e) {
                log.error("Error processing retry for member {}", member.getId(), e);
            }
        }
    }
}