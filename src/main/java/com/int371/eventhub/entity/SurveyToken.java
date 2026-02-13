package com.int371.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_tokens")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_used")
    private boolean isUsed = false;
}