package com.int371.eventhub.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackDataId implements Serializable {
    private Integer eventId;
    private String eventRole;
    private String surveysType;
    private String feedbackText;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiFeedbackDataId that = (AiFeedbackDataId) o;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(eventRole, that.eventRole) &&
               Objects.equals(surveysType, that.surveysType) &&
               Objects.equals(feedbackText, that.feedbackText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventRole, surveysType, feedbackText);
    }
}
