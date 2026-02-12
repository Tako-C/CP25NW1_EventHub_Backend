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

    @Scheduled(fixedRate = 60000) // run every 1 minute
    @Transactional
    public void updateEventStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 1. UPCOMING -> ONGOING
        // Find UPCOMING events that defined start date is passed
        List<Event> upcomingToOngoing = eventRepository.findAllByStartDateBeforeAndStatus(now, EventStatus.UPCOMING);
        if (!upcomingToOngoing.isEmpty()) {
            log.info("Found {} events to start (UPCOMING -> ONGOING)", upcomingToOngoing.size());
            upcomingToOngoing.forEach(e -> e.setStatus(EventStatus.ONGOING));
            eventRepository.saveAll(upcomingToOngoing);
        }

        // 2. ONGOING / UPCOMING -> FINISHED
        List<EventStatus> activeStatuses = List.of(EventStatus.UPCOMING, EventStatus.ONGOING);
        List<Event> expiredEvents = eventRepository.findAllByEndDateBeforeAndStatusIn(now, activeStatuses);

        if (!expiredEvents.isEmpty()) {
            log.info("Found {} events to update to FINISHED", expiredEvents.size());
            for (Event event : expiredEvents) {
                event.setStatus(EventStatus.FINISHED);
            }
            eventRepository.saveAll(expiredEvents);
            log.info("Updated {} events to FINISHED", expiredEvents.size());

            // 3. Send Post-Survey Email
            for (Event event : expiredEvents) {
                sendPostSurveyEmails(event);
            }
        }
    }

    private void sendPostSurveyEmails(Event event) {
        try {
            List<MemberEvent> members = memberEventRepository.findByEventId(event.getId());
            List<Survey> surveys = surveyRepository.findByEventIdAndStatus(event.getId(), SurveyStatus.ACTIVE);

            // Check if surveys exist
            boolean hasVisitorSurvey = false;
            boolean hasExhibitorSurvey = false;

            for (Survey s : surveys) {
                if (s.getType() == SurveyType.POST_VISITOR) {
                    hasVisitorSurvey = true;
                } else if (s.getType() == SurveyType.POST_EXHIBITOR) {
                    hasExhibitorSurvey = true;
                }
            }

            for (MemberEvent member : members) {
                boolean shouldSend = false;
                if (member.getEventRole() == MemberEventRole.VISITOR && hasVisitorSurvey) {
                    shouldSend = true;
                } else if (member.getEventRole() == MemberEventRole.EXHIBITOR && hasExhibitorSurvey) {
                    shouldSend = true;
                }

                if (shouldSend) {
                    emailService.sendPostSurveyEmail(
                            member.getUser().getEmail(),
                            member.getUser().getFirstName(),
                            event.getEventName(),
                            event.getId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send post-survey emails for event {}", event.getId(), e);
        }
    }
}
