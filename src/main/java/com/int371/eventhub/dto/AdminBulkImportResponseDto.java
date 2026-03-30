package com.int371.eventhub.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBulkImportResponseDto {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<String> successEmails;
    private List<FailedRow> failedRows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedRow {
        private int rowNumber;
        private String email;
        private String reason;
    }
}
