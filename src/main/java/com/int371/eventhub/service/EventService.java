// package com.int371.eventhub.service;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.nio.file.StandardCopyOption;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import com.int371.eventhub.dto.EditEventRequestDto;
// import com.int371.eventhub.dto.EventImageResponseDto;
// import com.int371.eventhub.dto.EventRequestDto;
// import com.int371.eventhub.dto.EventResponseDto;
// import com.int371.eventhub.entity.Event;
// import com.int371.eventhub.entity.EventType;
// import com.int371.eventhub.entity.EventImage;
// import com.int371.eventhub.entity.ImageCategory;
// import com.int371.eventhub.exception.ResourceNotFoundException;
// // import com.int371.eventhub.repository.EventImageRepository;
// import com.int371.eventhub.repository.EventRepository;
// import com.int371.eventhub.repository.EventTypeRepository;
// import com.int371.eventhub.repository.ImageCategoryRepository;

// import jakarta.transaction.Transactional;

// @Service
// public class EventService {

//     @Value("${app.upload.base-path:uploads/}")
//     private String uploadBaseDir;
//     @Autowired
//     private EventRepository eventRepository;

//     @Autowired
//     private EventTypeRepository eventTypeRepository;

//     // @Autowired
//     // private EventImageRepository eventImageRepository;

//     @Autowired
//     private ImageCategoryRepository categoryRepository;


//     @Autowired
//     private ModelMapper modelMapper;

//     private static final Set<String> CATEGORIES_FOR_ALL_EVENTS = Set.of("card", "slideshow");
//     private static final Set<String> CATEGORIES_FOR_EVENT_BY_ID = Set.of("detail", "map");

//     public List<EventResponseDto> getAllEvents() {
//     List<Event> events = eventRepository.findAll();

//     if (events.isEmpty()) {
//         throw new ResourceNotFoundException("No events available.");
//     }
    
//     return events.stream()
//                 .map(event -> convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_ALL_EVENTS))
//                 .toList();
//     }

//     public EventResponseDto getEventById(Integer id) {
//         Event event = eventRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

//         return convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);
    
//     }

//     private EventResponseDto convertEventToDtoWithImageStructure(Event event, Set<String> allowedCategoryNames) {
//         EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);

//         if (event.getImages() != null && !event.getImages().isEmpty()) {
//             Map<String, String> imageMap = event.getImages().stream()
//                 .filter(img -> img.getCategory() != null && img.getCategory().getCategoryName() != null)
//                 .map(img -> {
//                     img.getCategory().setCategoryName(img.getCategory().getCategoryName().toLowerCase());
//                     return img;
//                 })
//                 .filter(img -> allowedCategoryNames.contains(img.getCategory().getCategoryName()))
//                 .collect(Collectors.toMap(
//                     img -> img.getCategory().getCategoryName(),
//                     EventImage::getImgPathEv,
//                     (existingPath, newPath) -> existingPath 
//                 ));
//             EventImageResponseDto imageResponse = new EventImageResponseDto();
//             imageResponse.setImageCard(imageMap.get("card"));
//             imageResponse.setImageSlideShow(imageMap.get("slideshow"));
//             imageResponse.setImageDetail(imageMap.get("detail"));
//             imageResponse.setImageMap(imageMap.get("map"));
            
//             dto.setImages(imageResponse);
//         }

//         return dto;
//     }

//     @Transactional
//     public Event createEvent(EventRequestDto dto) {
//         // 1. แปลง DTO เป็น Entity
//         Event event = modelMapper.map(dto, Event.class);
//         event.setId(null);

//         // 2. Set Event Type
//         EventType eventType = eventTypeRepository.findById(dto.getEventTypeId())
//                 .orElseThrow(() -> new ResourceNotFoundException("EventType not found with id: " + dto.getEventTypeId()));
//         event.setEventTypeId(eventType);

//         // เตรียม List สำหรับเก็บรูปภาพ (ถ้าใน Entity ยังไม่มี)
//         if (event.getImages() == null) {
//             event.setImages(new ArrayList<>());
//         }
//         // 3. จัดการรูปภาพต่างๆ
//         // --- EVENT CARD ---
//         if (dto.getEventCard() != null && !dto.getEventCard().isEmpty()) {
//             event.getImages().add(prepareImageEntity(dto.getEventCard(), "Card", dto, event, null));
//         } else {
//             System.out.println(">>> Info: Event Card is null or empty. Skipping...");
//         }

//         // --- EVENT DETAIL ---
//         if (dto.getEventDetail() != null && !dto.getEventDetail().isEmpty()) {
//             event.getImages().add(prepareImageEntity(dto.getEventDetail(), "Detail", dto, event, null));
//         } else {
//             System.out.println(">>> Info: Event Detail is null or empty. Skipping...");
//         }

//         // --- EVENT MAP ---
//         if (dto.getEventMap() != null && !dto.getEventMap().isEmpty()) {
//             event.getImages().add(prepareImageEntity(dto.getEventMap(), "Map", dto, event, null));
//         } else {
//             System.out.println(">>> Info: Event Map is null or empty. Skipping...");
//         }

//     // --- EVENT SLIDESHOW ---
//     if (dto.getEventSlideshow() != null && !dto.getEventSlideshow().isEmpty()) {
//         int slideCount = 0;
//         for (int i = 0; i < dto.getEventSlideshow().size(); i++) {
//             MultipartFile slide = dto.getEventSlideshow().get(i);
//             if (slide != null && !slide.isEmpty()) {
//                 event.getImages().add(prepareImageEntity(slide, "Slideshow", dto, event, i + 1));
//                 slideCount++;
//             }
//         }
//         System.out.println(">>> Success: Processed " + slideCount + " slideshow images.");
//     } else {
//         System.out.println(">>> Info: Event Slideshow is null or empty. Skipping...");
//     }

//         // 4. บันทึกครั้งเดียว (Cascade จะช่วยบันทึกรูปภาพทั้งหมดให้เอง)
//         return eventRepository.save(event);
//     }

//     private EventImage prepareImageEntity(MultipartFile file, String type, EventRequestDto dto, Event event, Integer index) {
//         try {
//             String fileName = generateFileName(dto.getEventName(), dto.getStartDate(), type, file.getOriginalFilename(), index);
//             Path storageDirectory = Paths.get(uploadBaseDir);
//             if (!Files.exists(storageDirectory)) Files.createDirectories(storageDirectory);
            
//             Files.copy(file.getInputStream(), storageDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

//             EventImage eventImage = new EventImage();
//             eventImage.setUploadedAt(LocalDateTime.now());
//             eventImage.setImgPathEv(fileName);
            
//             eventImage.setEvent(event);

//             ImageCategory category = categoryRepository.findByCategoryName(type)
//                     .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + type));
//             eventImage.setCategory(category);

//             return eventImage; // คืนค่ากลับไปให้ createEvent แอดลง List
//         } catch (Exception e) {
//             throw new RuntimeException("Failed to process image [" + type + "]: " + e.getMessage(), e);
//         }
//     }

//     private String generateFileName(String eventName, LocalDateTime startDate, String type, String originalName, Integer index) {
//         // ลบอักขระพิเศษและเปลี่ยนเว้นวรรคเป็น underscore
//         String sanitizedName = eventName.toLowerCase().replaceAll("[^a-z0-9\\s]", "").replaceAll("\\s+", "_");
//         int year = (startDate != null) ? startDate.getYear() : LocalDateTime.now().getYear();

//         // ดึง Extension เดิม (เช่น .jpg, .png)
//         String extension = "";
//         if (originalName != null && originalName.contains(".")) {
//             extension = originalName.substring(originalName.lastIndexOf("."));
//         }
//         // รูปแบบ event_year_type_index.extension
//         if (index != null) {
//             return String.format("%s_%d_%s_%d%s", sanitizedName, year, type, index, extension);
//         }
//         return String.format("%s_%d_%s%s", sanitizedName, year, type, extension);

//     }

//     @Transactional
//     public Event updateEvent(Integer id, EditEventRequestDto dto) {
//         Event event = eventRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

//         // 1. อัปเดตข้อมูล Text ทั่วไป
//         event.setEventName(dto.getEventName());
//         event.setEventDesc(dto.getEventDesc());
//         EventType eventType = eventTypeRepository.findById(dto.getEventTypeId())
//                 .orElseThrow(() -> new ResourceNotFoundException("EventType not found with id: " + dto.getEventTypeId()));
//         event.setEventTypeId(eventType);
//         event.setLocation(dto.getLocation());
//         event.setHostOrganisation(dto.getHostOrganisation());
//         event.setCreatedBy(dto.getCreatedBy());
//         event.setContactLine(dto.getContactLine());
//         event.setContactEmail(dto.getContactEmail());
//         event.setContactFacebook(dto.getContactFacebook());
//         event.setContactPhone(dto.getContactPhone());
//         event.setStartDate(dto.getStartDate());
//         event.setEndDate(dto.getEndDate());

//         // 2. จัดการรูปภาพเดี่ยว (Card, Detail, Map)
//         // ถ้ามีการส่งไฟล์ใหม่มา ให้ลบรูปเก่าในประเภทนั้นออกก่อนแล้วเพิ่มรูปใหม่
//         if (dto.getEventCard() != null && !dto.getEventCard().isEmpty()) {
//             updateSingleImage(dto.getEventCard(), "Card", event);
//         }
//         if (dto.getEventDetail() != null && !dto.getEventDetail().isEmpty()) {
//             updateSingleImage(dto.getEventDetail(), "Detail", event);
//         }
//         if (dto.getEventMap() != null && !dto.getEventMap().isEmpty()) {
//             updateSingleImage(dto.getEventMap(), "Map", event);
//         }

//         // 3. จัดการ Slideshow แบบเจาะจง Index (1, 2, 3)
//         if (dto.getEventSlideshow() != null && dto.getSlideshowIndices() != null) {
//             // วนลูปตามจำนวนไฟล์ที่ส่งมา คู่กับ List ของ Index
//             int size = Math.min(dto.getEventSlideshow().size(), dto.getSlideshowIndices().size());
//             for (int i = 0; i < size; i++) {
//                 MultipartFile file = dto.getEventSlideshow().get(i);
//                 Integer targetIndex = dto.getSlideshowIndices().get(i);

//                 if (file != null && !file.isEmpty() && targetIndex >= 1 && targetIndex <= 3) {
//                     // ลบรูปสไลด์เดิมที่ Index นั้น (ถ้ามี)
//                     removeImageByCategoryAndIndex(event, "Slideshow", targetIndex);
//                     // เพิ่มรูปสไลด์ใหม่เข้าไปที่ Index เดิม
//                     event.getImages().add(prepareImageEntityForUpdate(file, "Slideshow", event, targetIndex));
//                 }
//             }
//         }

//         return eventRepository.save(event);
//     }

//     private void updateSingleImage(MultipartFile file, String category, Event event) {
//         // ลบรูปเดิมในหมวดนั้นๆ (Card, Detail, Map ไม่ต้องมี Index)
//         removeImageByCategoryAndIndex(event, category, null);
//         // เพิ่มรูปใหม่
//         event.getImages().add(prepareImageEntityForUpdate(file, category, event, null));
//     }

//     private void removeImageByCategoryAndIndex(Event event, String categoryName, Integer index) {
//         // สร้างเงื่อนไขการหาชื่อไฟล์: ถ้าเป็น Slideshow ต้องเช็ค _index ด้วย
//         String targetPattern = (index != null) ? "_" + categoryName + "_" + index + "." : "_" + categoryName + ".";

//         List<EventImage> toRemove = event.getImages().stream()
//                 .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase(categoryName))
//                 .filter(img -> img.getImgPathEv().contains(targetPattern))
//                 .collect(Collectors.toList());

//         for (EventImage img : toRemove) {
//             deleteImageFile(img.getImgPathEv()); // ลบไฟล์จริง
//             event.getImages().remove(img);       // ลบออกจาก List เพื่อให้ Cascade ลบใน DB
//         }
//     }

//     @Transactional
//     public void deleteEvent(Integer id) {
//         Event event = eventRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

//         //ดึงรายการรูปภาพทั้งหมดของ Event นี้ออกมาเพื่อลบไฟล์ใน Folder
//         if (event.getImages() != null && !event.getImages().isEmpty()) {
//             for (EventImage image : event.getImages()) {
//                 deleteImageFile(image.getImgPathEv());
//             }
//         }
//         eventRepository.delete(event);
//     }

//     private void deleteImageFile(String fileName) {
//         try {
//             Path filePath = Paths.get(uploadBaseDir).resolve(fileName);
//             if (Files.exists(filePath)) {
//                 Files.delete(filePath);
//                 System.out.println(">>> Deleted file: " + fileName);
//             } else {
//                 System.out.println(">>> File not found, skipping delete: " + fileName);
//             }
//         } catch (IOException e) {
//             System.err.println(">>> Error: Could not delete file " + fileName + " - " + e.getMessage());
//         }
//     }
// }



// package com.int371.eventhub.service;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.nio.file.StandardCopyOption;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import com.int371.eventhub.dto.EditEventRequestDto;
// import com.int371.eventhub.dto.EventImageResponseDto;
// import com.int371.eventhub.dto.EventRequestDto;
// import com.int371.eventhub.dto.EventResponseDto;
// import com.int371.eventhub.entity.Event;
// import com.int371.eventhub.entity.EventType;
// import com.int371.eventhub.entity.EventImage;
// import com.int371.eventhub.entity.ImageCategory;
// import com.int371.eventhub.exception.ResourceNotFoundException;
// import com.int371.eventhub.repository.EventRepository;
// import com.int371.eventhub.repository.EventTypeRepository;
// import com.int371.eventhub.repository.ImageCategoryRepository;

// import jakarta.transaction.Transactional;

// @Service
// public class EventService {

//     @Value("${app.upload.base-path:uploads/}")
//     private String uploadBaseDir;

//     @Autowired
//     private EventRepository eventRepository;

//     @Autowired
//     private EventTypeRepository eventTypeRepository;

//     @Autowired
//     private ImageCategoryRepository categoryRepository;

//     @Autowired
//     private ModelMapper modelMapper;

//     private static final Set<String> CATEGORIES_FOR_ALL_EVENTS = Set.of("card", "slideshow");
//     private static final Set<String> CATEGORIES_FOR_EVENT_BY_ID = Set.of("detail", "map", "card", "slideshow");

//     // --- ดึงข้อมูลทั้งหมด ---
//     public List<EventResponseDto> getAllEvents() {
//         List<Event> events = eventRepository.findAll();
//         if (events.isEmpty()) throw new ResourceNotFoundException("No events available.");
//         return events.stream()
//                 .map(event -> convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_ALL_EVENTS))
//                 .toList();
//     }

//     // --- ดึงข้อมูลตาม ID ---
//     public EventResponseDto getEventById(Integer id) {
//         Event event = eventRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
//         return convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);
//     }

//     // --- สร้าง Event ใหม่ ---
//     @Transactional
//     public Event createEvent(EventRequestDto dto) {
//         Event event = modelMapper.map(dto, Event.class);
//         event.setId(null);

//         EventType eventType = eventTypeRepository.findById(dto.getEventTypeId())
//                 .orElseThrow(() -> new ResourceNotFoundException("EventType not found with id: " + dto.getEventTypeId()));
//         event.setEventTypeId(eventType);

//         if (event.getImages() == null) event.setImages(new ArrayList<>());

//         // จัดการรูปเดี่ยว
//         if (isNotBlank(dto.getEventCard())) 
//             event.getImages().add(processImage(dto.getEventCard(), "Card", dto.getEventName(), dto.getStartDate(), event, null));
//         if (isNotBlank(dto.getEventDetail())) 
//             event.getImages().add(processImage(dto.getEventDetail(), "Detail", dto.getEventName(), dto.getStartDate(), event, null));
//         if (isNotBlank(dto.getEventMap())) 
//             event.getImages().add(processImage(dto.getEventMap(), "Map", dto.getEventName(), dto.getStartDate(), event, null));

//         // จัดการ Slideshow
//         if (dto.getEventSlideshow() != null && !dto.getEventSlideshow().isEmpty()) {
//             for (int i = 0; i < dto.getEventSlideshow().size(); i++) {
//                 MultipartFile slide = dto.getEventSlideshow().get(i);
//                 if (isNotBlank(slide)) {
//                     event.getImages().add(processImage(slide, "Slideshow", dto.getEventName(), dto.getStartDate(), event, i + 1));
//                 }
//             }
//         }
//         return eventRepository.save(event);
//     }

//     // --- แก้ไข Event ---
//   @Transactional
//     public Event updateEvent(Integer id, EditEventRequestDto dto) {
//         for (int index = 0; index < dto.getSlideshowIndices().size(); index++) {
//             Integer targetIndex = dto.getSlideshowIndices().get(index);
//             if (targetIndex < 1 || targetIndex > 3) {
//                 throw new IllegalArgumentException("Slideshow index must be between 1 and 3. Invalid index: " + targetIndex);
//             }
//         }
//         // 1. ดึงของเก่าจาก DB
//         Event event = eventRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

//         // 2. Map ข้อมูลพื้นฐาน (String, LocalDateTime, Integer ทั่วไป)
//         // *หาก ModelMapper ยังพยายามเปลี่ยน ID ของ EventType ให้ใช้การ set ทีละฟิลด์แทน*
//         event.setEventName(dto.getEventName());
//         event.setEventDesc(dto.getEventDesc());
//         event.setLocation(dto.getLocation());
//         event.setHostOrganisation(dto.getHostOrganisation());
//         event.setContactEmail(dto.getContactEmail());
//         event.setContactFacebook(dto.getContactFacebook());
//         event.setContactLine(dto.getContactLine());
//         event.setContactPhone(dto.getContactPhone());
//         event.setStartDate(dto.getStartDate());
//         event.setEndDate(dto.getEndDate());
//         event.setCreatedBy(dto.getCreatedBy());

//         // 3. จัดการเปลี่ยน Event Type (Relationship) - ห้ามใช้ ModelMapper ในส่วนนี้
//         if (dto.getEventTypeId() != null) {
//             EventType newType = eventTypeRepository.findById(dto.getEventTypeId())
//                     .orElseThrow(() -> new ResourceNotFoundException("Type not found"));
//             event.setEventTypeId(newType); // เปลี่ยน Object ทั้งก้อน ไม่ใช่เปลี่ยนแค่ ID
//         }

//         // 4. ส่วนการจัดการรูปภาพ (ใช้ Code เดิมที่เขียนไว้)
//         if (isNotBlank(dto.getEventCard())) updateSingleImage(dto.getEventCard(), "Card", event);
//         if (isNotBlank(dto.getEventDetail())) updateSingleImage(dto.getEventDetail(), "Detail", event);
//         if (isNotBlank(dto.getEventMap())) updateSingleImage(dto.getEventMap(), "Map", event);

//         if (dto.getEventSlideshow() != null && dto.getSlideshowIndices() != null) {
//             int size = Math.min(dto.getEventSlideshow().size(), dto.getSlideshowIndices().size());
//             for (int i = 0; i < size; i++) {
//                 MultipartFile file = dto.getEventSlideshow().get(i);
//                 Integer targetIndex = dto.getSlideshowIndices().get(i);
//                 if (isNotBlank(file) && targetIndex >= 1 && targetIndex <= 3) {
//                     removeImageByCategoryAndIndex(event, "Slideshow", targetIndex);
//                     event.getImages().add(processImage(file, "Slideshow", event.getEventName(), event.getStartDate(), event, targetIndex));
//                 }
//             }
//         }

//         return eventRepository.save(event);
//     }
//     // --- ลบ Event ---
//     @Transactional
//     public void deleteEvent(Integer id) {
//         Event event = eventRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
//         if (event.getImages() != null) {
//             for (EventImage image : event.getImages()) {
//                 deleteImageFile(image.getImgPathEv());
//             }
//         }
//         eventRepository.delete(event);
//     }

//     // --- HELPER METHODS ---

//     // **จุดรวมร่าง: ใช้ตัวเดียวได้ทั้ง Create และ Update**
//     private EventImage processImage(MultipartFile file, String type, String eventName, LocalDateTime startDate, Event event, Integer index) {
//         try {
//             String fileName = generateFileName(eventName, startDate, type, file.getOriginalFilename(), index);
//             Path storageDirectory = Paths.get(uploadBaseDir);
//             if (!Files.exists(storageDirectory)) Files.createDirectories(storageDirectory);
            
//             Files.copy(file.getInputStream(), storageDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

//             EventImage eventImage = new EventImage();
//             eventImage.setUploadedAt(LocalDateTime.now());
//             eventImage.setImgPathEv(fileName);
//             eventImage.setEvent(event);
//             eventImage.setCategory(categoryRepository.findByCategoryName(type)
//                     .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + type)));

//             return eventImage;
//         } catch (IOException e) {
//             throw new RuntimeException("Failed to process image [" + type + "]: " + e.getMessage());
//         }
//     }

//     private void updateSingleImage(MultipartFile file, String category, Event event) {
//         removeImageByCategoryAndIndex(event, category, null);
//         event.getImages().add(processImage(file, category, event.getEventName(), event.getStartDate(), event, null));
//     }

//     private void removeImageByCategoryAndIndex(Event event, String categoryName, Integer index) {
//         String targetPattern = (index != null) ? "_" + categoryName + "_" + index + "." : "_" + categoryName + ".";
//         List<EventImage> toRemove = event.getImages().stream()
//                 .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase(categoryName))
//                 .filter(img -> img.getImgPathEv().contains(targetPattern))
//                 .collect(Collectors.toList());

//         for (EventImage img : toRemove) {
//             deleteImageFile(img.getImgPathEv());
//             event.getImages().remove(img);
//         }
//     }

//     private void deleteImageFile(String fileName) {
//         try {
//             Path filePath = Paths.get(uploadBaseDir).resolve(fileName);
//             Files.deleteIfExists(filePath);
//             System.out.println(">>> Deleted file: " + fileName);
//         } catch (IOException e) {
//             System.err.println(">>> Error: " + e.getMessage());
//         }
//     }

//     private String generateFileName(String eventName, LocalDateTime startDate, String type, String originalName, Integer index) {
//         String sanitizedName = eventName.toLowerCase().replaceAll("[^a-z0-9]", "_");
//         int year = (startDate != null) ? startDate.getYear() : LocalDateTime.now().getYear();
//         String extension = originalName.substring(originalName.lastIndexOf("."));
        
//         if (index != null) return String.format("%s_%d_%s_%d%s", sanitizedName, year, type, index, extension);
//         return String.format("%s_%d_%s%s", sanitizedName, year, type, extension);
//     }

//     private boolean isNotBlank(MultipartFile file) {
//         return file != null && !file.isEmpty();
//     }

//     private EventResponseDto convertEventToDtoWithImageStructure(Event event, Set<String> allowedCategoryNames) {
//         EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);
//         if (event.getImages() != null && !event.getImages().isEmpty()) {
//             Map<String, String> imageMap = event.getImages().stream()
//                 .filter(img -> img.getCategory() != null)
//                 .filter(img -> allowedCategoryNames.contains(img.getCategory().getCategoryName().toLowerCase()))
//                 .collect(Collectors.toMap(
//                     img -> img.getCategory().getCategoryName().toLowerCase(),
//                     EventImage::getImgPathEv,
//                     (existing, replacement) -> existing 
//                 ));
//             EventImageResponseDto imageResponse = new EventImageResponseDto();
//             imageResponse.setImageCard(imageMap.get("card"));
//             imageResponse.setImageSlideShow(imageMap.get("slideshow"));
//             imageResponse.setImageDetail(imageMap.get("detail"));
//             imageResponse.setImageMap(imageMap.get("map"));
//             dto.setImages(imageResponse);
//         }
//         return dto;
//     }
// }



package com.int371.eventhub.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// import java.util.Map;
import java.util.Set;
// import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.int371.eventhub.dto.EditEventRequestDto;
import com.int371.eventhub.dto.EventImageResponseDto;
import com.int371.eventhub.dto.EventRequestDto;
import com.int371.eventhub.dto.EventResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.EventImage;
import com.int371.eventhub.entity.EventType;
import com.int371.eventhub.entity.ImageCategory;
import com.int371.eventhub.exception.ResourceNotFoundException;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.EventTypeRepository;
import com.int371.eventhub.repository.ImageCategoryRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {

    @Value("${app.upload.base-path:uploads/}")
    private String uploadBaseDir;

    @Autowired private EventRepository eventRepository;
    @Autowired private EventTypeRepository eventTypeRepository;
    @Autowired private ImageCategoryRepository categoryRepository;
    @Autowired private ModelMapper modelMapper;

    private static final Set<String> CATEGORIES_FOR_ALL_EVENTS = Set.of("card", "slideshow");
    private static final Set<String> CATEGORIES_FOR_EVENT_BY_ID = Set.of("card", "slideshow", "detail", "map");

    // ========================= GET =========================

    public List<EventResponseDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        if (events.isEmpty()) throw new ResourceNotFoundException("No events available.");
        return events.stream()
                .map(e -> convertEventToDtoWithImageStructure(e, CATEGORIES_FOR_ALL_EVENTS))
                .toList();
    }

    public EventResponseDto getEventById(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return convertEventToDtoWithImageStructure(event, CATEGORIES_FOR_EVENT_BY_ID);
    }

    // ========================= CREATE =========================

    @Transactional
    public Event createEvent(EventRequestDto dto) {
        // VALIDATE slideshow (ห้ามเกิน 3)
        if (dto.getEventSlideshow() != null && dto.getEventSlideshow().size() > 3) {
            throw new IllegalArgumentException("Event slideshow can contain at most 3 images");
        }

        Event event = modelMapper.map(dto, Event.class);
        event.setId(null);

        EventType type = eventTypeRepository.findById(dto.getEventTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EventType not found"));
        event.setEventTypeId(type);

        if (event.getImages() == null) event.setImages(new ArrayList<>());

        // 👉 save รอบแรก เพื่อให้ได้ eventId
        event = eventRepository.save(event);
        Integer eventId = event.getId();

        if (isNotBlank(dto.getEventCard()))
            event.getImages().add(processImage(dto.getEventCard(), "card", eventId, event, null));

        if (isNotBlank(dto.getEventDetail()))
            event.getImages().add(processImage(dto.getEventDetail(), "detail", eventId, event, null));

        if (isNotBlank(dto.getEventMap()))
            event.getImages().add(processImage(dto.getEventMap(), "map", eventId, event, null));

        if (dto.getEventSlideshow() != null) {
            for (int i = 0; i < dto.getEventSlideshow().size(); i++) {
                MultipartFile file = dto.getEventSlideshow().get(i);
                if (isNotBlank(file)) {
                    event.getImages().add(
                        processImage(file, "slideshow", eventId, event, i + 1)
                    );
                }
            }
        }

        return eventRepository.save(event);
    }

    // ========================= UPDATE =========================

    @Transactional
    public Event updateEvent(Integer id, EditEventRequestDto dto) {
        for (int index = 0; index < dto.getSlideshowIndices().size(); index++) {
            Integer targetIndex = dto.getSlideshowIndices().get(index);
            if (targetIndex < 1 || targetIndex > 3) {
                throw new IllegalArgumentException("Slideshow index must be between 1 and 3. Invalid index: " + targetIndex);
            }
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Integer eventId = event.getId();

        event.setEventName(dto.getEventName());
        event.setEventDesc(dto.getEventDesc());
        event.setLocation(dto.getLocation());
        event.setHostOrganisation(dto.getHostOrganisation());
        event.setContactEmail(dto.getContactEmail());
        event.setContactFacebook(dto.getContactFacebook());
        event.setContactLine(dto.getContactLine());
        event.setContactPhone(dto.getContactPhone());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setCreatedBy(dto.getCreatedBy());

        if (dto.getEventTypeId() != null) {
            EventType type = eventTypeRepository.findById(dto.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventType not found"));
            event.setEventTypeId(type);
        }

        if (isNotBlank(dto.getEventCard()))
            updateSingleImage(dto.getEventCard(), "card", eventId, event);

        if (isNotBlank(dto.getEventDetail()))
            updateSingleImage(dto.getEventDetail(), "detail", eventId, event);

        if (isNotBlank(dto.getEventMap()))
            updateSingleImage(dto.getEventMap(), "map", eventId, event);

        if (dto.getEventSlideshow() != null && dto.getSlideshowIndices() != null) {
            int size = Math.min(dto.getEventSlideshow().size(), dto.getSlideshowIndices().size());
            for (int i = 0; i < size; i++) {
                MultipartFile file = dto.getEventSlideshow().get(i);
                Integer index = dto.getSlideshowIndices().get(i);
                if (isNotBlank(file)) {
                    removeImageByCategoryAndIndex(event, "slideshow", index);
                    event.getImages().add(
                        processImage(file, "slideshow", eventId, event, index)
                    );
                }
            }
        }

        return eventRepository.save(event);
    }

    // ========================= DELETE =========================
    @Transactional
    public void deleteEvent(Integer id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // 1. ลบไฟล์รูปทั้งหมด
        if (event.getImages() != null) {
            for (EventImage img : event.getImages()) {
                deleteImageFile(img.getImgPathEv());
            }
        }

        // 2. ลบ Event (cascade ลบ images ใน DB)
        eventRepository.delete(event);
    }

    @Transactional
    public void deleteEventImage(Integer eventId, String category, Integer index) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        category = category.toLowerCase();

        if ("slideshow".equals(category)) {
            if (index == null) {
                throw new IllegalArgumentException("Slideshow image requires index");
            }
            deleteSlideshowImage(event, index);
        } else {
            deleteSingleImage(event, category);
        }
    }



    // ========================= IMAGE CORE =========================

    private EventImage processImage(
            MultipartFile file,
            String category,
            Integer eventId,
            Event event,
            Integer index
    ) {
        try {
            String fileName = generateFileName(eventId, category, index, file.getOriginalFilename());

            Path dir = Paths.get(uploadBaseDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            EventImage img = new EventImage();
            img.setUploadedAt(LocalDateTime.now());
            img.setImgPathEv(fileName);
            img.setEvent(event);

            ImageCategory cat = categoryRepository.findByCategoryName(category)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + category));
            img.setCategory(cat);

            return img;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    private String generateFileName(Integer eventId, String category, Integer index, String originalName) {
        String ext = originalName.substring(originalName.lastIndexOf("."));
        if (index != null)
            return String.format("%d_%s_%d%s", eventId, category, index, ext);
        return String.format("%d_%s%s", eventId, category, ext);
    }

    private void updateSingleImage(MultipartFile file, String category, Integer eventId, Event event) {
        removeImageByCategoryAndIndex(event, category, null);
        event.getImages().add(processImage(file, category, eventId, event, null));
    }

    private void removeImageByCategoryAndIndex(Event event, String category, Integer index) {
        String pattern = (index != null) ? "_" + category + "_" + index + "." : "_" + category + ".";
        List<EventImage> toRemove = event.getImages().stream()
                .filter(i -> i.getImgPathEv().contains(pattern))
                .toList();

        for (EventImage img : toRemove) {
            deleteImageFile(img.getImgPathEv());
            event.getImages().remove(img);
        }
    }

    private void deleteImageFile(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(uploadBaseDir).resolve(fileName));
        } catch (IOException ignored) {}
    }

    private boolean isNotBlank(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void deleteSingleImage(Event event, String category) {
        boolean removed = event.getImages().removeIf(img -> {
            boolean match = img.getCategory()
                    .getCategoryName()
                    .equalsIgnoreCase(category);

            if (match) {
                deleteImageFile(img.getImgPathEv());
            }
            return match;
        });
        if (!removed) {
            throw new ResourceNotFoundException("Image not found: " + category);
        }
    }


    private void deleteSlideshowImage(Event event, int index) {
        String pattern = "_slideshow_" + index + ".";
        EventImage target = event.getImages().stream()
                .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("slideshow"))
                .filter(img -> img.getImgPathEv().contains(pattern))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Slideshow image not found at index " + index));

        deleteImageFile(target.getImgPathEv());
        event.getImages().remove(target);
    }



    // ========================= DTO MAP =========================

    private EventResponseDto convertEventToDtoWithImageStructure(
        Event event,
        Set<String> allowedCategoryNames) {

        EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);

        if (event.getImages() == null || event.getImages().isEmpty()) {
            return dto;
        }

        EventImageResponseDto imageDto = new EventImageResponseDto();

        // CARD
        event.getImages().stream()
            .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("Card"))
            .findFirst()
            .ifPresent(img -> imageDto.setImageCard(img.getImgPathEv()));

        // DETAIL
        event.getImages().stream()
            .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("Detail"))
            .findFirst()
            .ifPresent(img -> imageDto.setImageDetail(img.getImgPathEv()));

        // MAP
        event.getImages().stream()
            .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("map"))
            .findFirst()
            .ifPresent(img -> imageDto.setImageMap(img.getImgPathEv()));

        // SLIDESHOW (LIST)
        List<String> slideshow = event.getImages().stream()
            .filter(img -> img.getCategory().getCategoryName().equalsIgnoreCase("slideshow"))
            .sorted((a, b) -> a.getImgPathEv().compareTo(b.getImgPathEv())) // เรียง 1,2,3
            .map(EventImage::getImgPathEv)
            .toList();

        imageDto.setImageSlideShow(slideshow);

        dto.setImages(imageDto);
        return dto;
    }

    // private EventResponseDto convertEventToDtoWithImageStructure(Event event, Set<String> allowed) {
    //     EventResponseDto dto = modelMapper.map(event, EventResponseDto.class);

    //     if (event.getImages() != null) {
    //         Map<String, String> map = event.getImages().stream()
    //             .filter(i -> allowed.contains(i.getCategory().getCategoryName().toLowerCase()))
    //             .collect(Collectors.toMap(
    //                 i -> i.getCategory().getCategoryName().toLowerCase(),
    //                 EventImage::getImgPathEv,
    //                 (a, b) -> a
    //             ));

    //         EventImageResponseDto img = new EventImageResponseDto();
    //         img.setImageCard(map.get("card"));
    //         img.setImageSlideShow(map.get("slideshow"));
    //         img.setImageDetail(map.get("detail"));
    //         img.setImageMap(map.get("map"));
    //         dto.setImages(img);
    //     }
    //     return dto;
    // }
}
