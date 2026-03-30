package com.int371.eventhub.service;

import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// import org.apache.commons.csv.CSVFormat;
// import org.apache.commons.csv.CSVParser;
// import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.int371.eventhub.dto.AdminBulkImportResponseDto;
import com.int371.eventhub.entity.Event;
import com.int371.eventhub.entity.MemberEventRole;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.entity.UserRole;
import com.int371.eventhub.entity.UserStatus;
import com.int371.eventhub.repository.EventRepository;
import com.int371.eventhub.repository.MemberEventRepository;
import com.int371.eventhub.repository.UserRepository;

@Service
public class AdminBulkImportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MemberEventRepository memberEventRepository;

    @Autowired
    private EventRegistrationService eventRegistrationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    private static class ParsedRowRecord {
        int rowNumber;
        String email;
        String firstName;
        String lastName;
        String dbGender;
        LocalDate dob;
        MemberEventRole role;
    }

    @Transactional
    public AdminBulkImportResponseDto importUsers(Integer eventId, MultipartFile file) {
        AdminBulkImportResponseDto response = new AdminBulkImportResponseDto();
        response.setFailedRows(new ArrayList<>());
        response.setSuccessEmails(new ArrayList<>());

        String filename = file.getOriginalFilename();
        if (filename == null) {
            addError(response, 0, "Unknown", "File name is missing");
            return response;
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            addError(response, 0, "Global", "Event not found with ID: " + eventId);
            return response;
        }

        List<ParsedRowRecord> parsedRecords = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
                extractExcel(is, response, parsedRecords);
                // } else if (filename.toLowerCase().endsWith(".csv")) {
                // extractCsv(is, response, parsedRecords);
            } else {
                addError(response, 0, "Unknown", "Unsupported file type. Use .csv or .xlsx");
                return response;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse file structure: " + e.getMessage());
        }

        Set<String> seenEmailsInFile = new HashSet<>();
        for (ParsedRowRecord record : parsedRecords) {
            if (!seenEmailsInFile.add(record.email.toLowerCase())) {
                addError(response, record.rowNumber, record.email,
                        "Duplicate email found within the uploaded file itself.");
            }
        }

        if (response.getFailedRows().isEmpty() && !parsedRecords.isEmpty()) {
            for (ParsedRowRecord record : parsedRecords) {
                User user = userRepository.findByEmailIgnoreCase(record.email).orElse(null);
                if (user != null) {
                    if (memberEventRepository.existsByUserIdAndEventId(user.getId(), event.getId())) {
                        addError(response, record.rowNumber, record.email,
                                "User is already registered for this event.");
                    }
                }
            }
        }

        // ALL-OR-NOTHING ABORT
        if (!response.getFailedRows().isEmpty()) {
            // Do not save anything.
            response.setSuccessCount(0);
            return response;
        }

        // EXECUTION PHASE (No Errors Found)
        for (ParsedRowRecord record : parsedRecords) {
            User user = userRepository.findByEmailIgnoreCase(record.email).orElse(null);

            if (user == null) {
                user = new User();
                user.setEmail(record.email);
                user.setFirstName(record.firstName);
                user.setLastName(record.lastName);
                user.setGender(record.dbGender);
                user.setDateOfBirth(record.dob);
                user.setRole(UserRole.GENERAL_USER);
                user.setStatus(UserStatus.ACTIVE);

                String rawPassword = authService.generateRandomPassword();
                user.setPassword(passwordEncoder.encode(rawPassword));

                user = userRepository.save(user);
                authService.sendWelcomeEmailAfterRegistration(user, rawPassword, true);
            }

            // Register to Event
            eventRegistrationService.registerUserForEvent(user, event, record.role);

            response.getSuccessEmails().add(record.email);
        }

        response.setSuccessCount(parsedRecords.size());
        return response;
    }

    // private void extractCsv(InputStream is, AdminBulkImportResponseDto response,
    // List<ParsedRowRecord> records)
    // throws Exception {
    // try (BufferedReader reader = new BufferedReader(new InputStreamReader(is,
    // "UTF-8"));
    // CSVParser csvParser = new CSVParser(reader,
    // CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim()))
    // {

    // MemberEventRole defaultRole = MemberEventRole.VISITOR;
    // int currentRow = 1;
    // for (CSVRecord record : csvParser) {
    // currentRow++;
    // String email = getMapped(record, "email");
    // String firstName = getMapped(record, "firstName", "first name", "firstname");
    // String lastName = getMapped(record, "lastName", "last name", "lastname");
    // String gender = getMapped(record, "gender");
    // String dateOfBirth = getMapped(record, "dateOfBirth", "date of birth",
    // "dateofbirth");

    // if ((email == null || email.trim().isEmpty()) &&
    // (firstName == null || firstName.trim().isEmpty()) &&
    // (lastName == null || lastName.trim().isEmpty()) &&
    // (gender == null || gender.trim().isEmpty()) &&
    // (dateOfBirth == null || dateOfBirth.trim().isEmpty())) {
    // continue; // Skip completely empty trailing lines
    // }

    // parseAndValidateRow(
    // email, firstName, lastName, gender, dateOfBirth,
    // defaultRole, currentRow, response, records);
    // }
    // }
    // }

    // private String getMapped(CSVRecord record, String... keys) {
    // for (String key : keys) {
    // if (record.isMapped(key))
    // return record.get(key);
    // }
    // return null;
    // }

    private void extractExcel(InputStream is, AdminBulkImportResponseDto response, List<ParsedRowRecord> records)
            throws Exception {
        try (Workbook workbook = WorkbookFactory.create(is)) {

            for (int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
                Sheet sheet = workbook.getSheetAt(sheetIdx);
                String sheetName = sheet.getSheetName().trim().toUpperCase();

                MemberEventRole role = mapSheetNameToRole(sheetName);
                if (role == null)
                    continue;

                Row headerRow = sheet.getRow(0);
                if (headerRow == null)
                    continue;

                int emailIdx = -1, fnIdx = -1, lnIdx = -1, genderIdx = -1, dobIdx = -1;
                for (Cell cell : headerRow) {
                    String header = cell.getStringCellValue().trim().toLowerCase();
                    switch (header) {
                        case "email":
                            emailIdx = cell.getColumnIndex();
                            break;
                        case "first name":
                        case "firstname":
                            fnIdx = cell.getColumnIndex();
                            break;
                        case "last name":
                        case "lastname":
                            lnIdx = cell.getColumnIndex();
                            break;
                        case "gender":
                            genderIdx = cell.getColumnIndex();
                            break;
                        case "date of birth":
                        case "dateofbirth":
                            dobIdx = cell.getColumnIndex();
                            break;
                    }
                }

                if (emailIdx == -1)
                    continue;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    String email = getCellValueAsString(row.getCell(emailIdx));
                    String firstName = getCellValueAsString(row.getCell(fnIdx));
                    String lastName = getCellValueAsString(row.getCell(lnIdx));
                    String gender = getCellValueAsString(row.getCell(genderIdx));
                    String dateOfBirth = getCellValueAsString(row.getCell(dobIdx));

                    if ((email == null || email.trim().isEmpty()) &&
                            (firstName == null || firstName.trim().isEmpty()) &&
                            (lastName == null || lastName.trim().isEmpty()) &&
                            (gender == null || gender.trim().isEmpty()) &&
                            (dateOfBirth == null || dateOfBirth.trim().isEmpty())) {
                        continue; // Skip entirely empty row
                    }

                    parseAndValidateRow(email, firstName, lastName, gender, dateOfBirth, role, i + 1, response,
                            records);
                }
            }
        }
    }

    private MemberEventRole mapSheetNameToRole(String sheetName) {
        switch (sheetName) {
            case "STAFF":
                return MemberEventRole.STAFF;
            case "EXHIBITOR":
                return MemberEventRole.EXHIBITOR;
            case "ORGANIZER":
                return MemberEventRole.ORGANIZER;
            case "VISITOR":
                return MemberEventRole.VISITOR;
            default:
                return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                java.time.LocalDate ld = cell.getLocalDateTimeCellValue().toLocalDate();
                return ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                long val = (long) cell.getNumericCellValue();
                return String.valueOf(val);
            }
        }
        return cell.toString().trim();
    }

    private void parseAndValidateRow(String email, String firstName, String lastName, String gender, String dateOfBirth,
            MemberEventRole role, int rowNum, AdminBulkImportResponseDto response, List<ParsedRowRecord> records) {

        response.setTotalRows(response.getTotalRows() + 1);
        boolean hasError = false;

        if (email == null || email.trim().isEmpty()) {
            addError(response, rowNum, email, "Email is perfectly blank or missing.");
            hasError = true;
        } else if (!email.trim().matches(EMAIL_REGEX)) {
            addError(response, rowNum, email, "Invalid Email format.");
            hasError = true;
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            addError(response, rowNum, email, "First Name is missing or empty.");
            hasError = true;
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            addError(response, rowNum, email, "Last Name is missing or empty.");
            hasError = true;
        }

        String dbGender = null;
        if (gender == null || gender.trim().isEmpty()) {
            addError(response, rowNum, email, "Gender is missing or empty.");
            hasError = true;
        } else {
            String g = gender.trim().toUpperCase();
            if (g.equals("M") || g.equals("MALE"))
                dbGender = "M";
            else if (g.equals("F") || g.equals("FEMALE"))
                dbGender = "F";
            else if (g.equals("U") || g.equals("UNKNOWN"))
                dbGender = "U";
            else if (g.equals("N") || g.equals("NONE"))
                dbGender = "N";
            else {
                addError(response, rowNum, email,
                        "Invalid Gender value. Only M, F, U, N (or Male/Female) are permitted. Received: " + gender);
                hasError = true;
            }
        }

        LocalDate parsedDob = null;
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            addError(response, rowNum, email, "Date of Birth is missing or empty.");
            hasError = true;
        } else {
            parsedDob = parseDate(dateOfBirth);
            if (parsedDob == null) {
                addError(response, rowNum, email,
                        "Invalid Date of Birth format. Please use DD/MM/YYYY. Received: " + dateOfBirth);
                hasError = true;
            }
        }

        if (!hasError) {
            ParsedRowRecord record = new ParsedRowRecord();
            record.rowNumber = rowNum;
            record.email = email != null ? email.trim() : "";
            record.firstName = firstName != null ? firstName.trim() : "";
            record.lastName = lastName != null ? lastName.trim() : "";
            record.dbGender = dbGender;
            record.dob = parsedDob;
            record.role = role;
            records.add(record);
        }
    }

    private LocalDate parseDate(String dobStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dobStr.trim(), formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void addError(AdminBulkImportResponseDto response, int rowNum, String email, String reason) {
        response.getFailedRows()
                .add(new AdminBulkImportResponseDto.FailedRow(rowNum, email != null ? email : "N/A", reason));
        response.setFailedCount(response.getFailedCount() + 1);
    }
}
