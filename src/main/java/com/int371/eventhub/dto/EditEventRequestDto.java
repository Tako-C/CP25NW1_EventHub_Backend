package com.int371.eventhub.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class EditEventRequestDto {
    private String eventName;
    private String eventDesc;
    private Integer eventTypeId;
    private String location;
    private String hostOrganisation;
    private Integer createdBy;
    private String contactLine;
    private String contactEmail;
    private String contactFacebook;
    private String contactPhone;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private MultipartFile eventCard;
    private MultipartFile eventDetail;
    private MultipartFile eventMap;

    // รับไฟล์ Slideshow เป็น List
    private List<MultipartFile> eventSlideshow; 
    
    // รับลำดับ Index ที่ต้องการเปลี่ยน (เช่น [1, 3] หากต้องการเปลี่ยนรูปที่ 1 และ 3)
    private List<Integer> slideshowIndices; 
}
