package com.int371.eventhub.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class EventRequestDto {
    @NotBlank(message = "Event name is required")
    private String eventName;
    private String eventDesc;
    @NotNull(message = "Event type is required")
    private Integer eventTypeId; // รับเป็น ID เพื่อความง่ายในการ Map
    private String location;
    private String hostOrganisation;
    private Integer createdBy;
    private String contactLine;
    @NotBlank(message = "Contact email is required")
    private String contactEmail;
    private String contactFacebook;
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // เพิ่มฟิลด์สำหรับรับไฟล์รูปภาพ
    private MultipartFile eventCard;       // รูปแผนที่ 1 รูป
    private MultipartFile eventDetail;    // รูปรายละเอียด 1 รูป
    private MultipartFile eventMap;       // รูปแผนที่
    private List<MultipartFile> eventSlideshow; // รูปสไลด์โชว์ (ส่งมาได้หลายรูป)
}
