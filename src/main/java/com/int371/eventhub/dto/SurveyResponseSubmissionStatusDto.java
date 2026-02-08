package com.int371.eventhub.dto;

import com.int371.eventhub.entity.SubmissionSurveyStatus;
import com.int371.eventhub.entity.SurveyType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurveyResponseSubmissionStatusDto {

    private Integer memberEventId;
    private String firstName;
    private String lastName;
    private String eventName;
    private SurveyType surveyType;
    private SubmissionSurveyStatus status;

    // 🔥 constructor ต้อง public และ parameter ต้องตรง
    public SurveyResponseSubmissionStatusDto(
            Integer memberEventId,
            String firstName,
            String lastName,
            String eventName,
            SurveyType surveyType,
            Boolean submitted
    ) {
        this.memberEventId = memberEventId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.eventName = eventName;
        this.surveyType = surveyType;
        this.status = submitted
                ? SubmissionSurveyStatus.SUBMITTED
                : SubmissionSurveyStatus.PENDING;
    }

    // getters
}
