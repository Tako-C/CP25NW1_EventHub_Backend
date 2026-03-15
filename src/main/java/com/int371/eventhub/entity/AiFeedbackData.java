package com.int371.eventhub.entity;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Immutable
@Table(name = "V_AI_FEEDBACK_DATA")
@IdClass(AiFeedbackDataId.class)
public class AiFeedbackData {

    @Id
    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Id
    @Column(name = "EVENT_ROLE")
    private String eventRole;

    @Id
    @Column(name = "SURVEYS_TYPE")
    private String surveysType;

    @Id
    @Column(name = "FEEDBACK_TEXT")
    private String feedbackText;

}
