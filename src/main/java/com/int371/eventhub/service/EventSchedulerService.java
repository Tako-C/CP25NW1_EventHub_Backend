package com.int371.eventhub.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.int371.eventhub.entity.SurveyToken;
import com.int371.eventhub.entity.SurveyType;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.SurveyRepository;
import com.int371.eventhub.repository.SurveyTokenRepository;

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
    private SurveyTokenRepository surveyTokenRepository;
    @Autowired
    private EmailService emailService;

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

    private void sendPostSurveyEmails(Event event) {
        try {
            List<MemberEvent> members = memberEventRepository.findByEventId(event.getId());
            List<Survey> surveys = surveyRepository.findByEventIdAndStatus(event.getId(), SurveyStatus.ACTIVE);

            // ตรวจสอบว่ามี Post-Survey ไหม
            boolean hasVisitorSurvey = surveys.stream().anyMatch(s -> s.getType() == SurveyType.POST_VISITOR);
            boolean hasExhibitorSurvey = surveys.stream().anyMatch(s -> s.getType() == SurveyType.POST_EXHIBITOR);

            for (MemberEvent member : members) {
                if (shouldSendEmail(member, hasVisitorSurvey, hasExhibitorSurvey)) {
                    

                    Optional<SurveyToken> existingToken = surveyTokenRepository.findByUserIdAndEventId(
                        member.getUser().getId(), 
                        event.getId()
                    );

                    if (existingToken.isPresent()) {
                        continue; 
                    }

                    createNewTokenAndSendEmail(member, event);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send post-survey emails for event {}", event.getId(), e);
        }
    }

    @Transactional
    protected void createNewTokenAndSendEmail(MemberEvent member, Event event) {
        try {
            SurveyToken surveyToken = new SurveyToken();
            surveyToken.setUser(member.getUser());
            surveyToken.setEvent(event);
            surveyToken.setExpiryDate(LocalDateTime.now().plusDays(7));
            surveyToken.setUsed(false);
            
            surveyToken = surveyTokenRepository.save(surveyToken);

            emailService.sendPostSurveyEmail(
                    member.getUser().getEmail(),
                    member.getUser().getFirstName(),
                    event.getEventName(),
                    event.getId(),
                    surveyToken.getToken()
            );
        } catch (Exception e) {
            log.error("Error creating token for user {}", member.getUser().getEmail(), e);
        }
    }

    private boolean shouldSendEmail(MemberEvent member, boolean hasVisitor, boolean hasExhibitor) {
        return (member.getEventRole() == MemberEventRole.VISITOR && hasVisitor) ||
               (member.getEventRole() == MemberEventRole.EXHIBITOR && hasExhibitor);
    }
}