package com.int371.eventhub.dto;

import lombok.Data;

@Data
public class CheckInPreviewResponseDto {
    private UserPreviewDto userProfile;
    private EventPreviewDto eventDetail;

    @Data
    public static class UserPreviewDto {
        private String firstName;
        private String lastName;
        private String imgPath;
        private String email;
    }

    @Data
    public static class EventPreviewDto {
        private String eventName;
    }
}