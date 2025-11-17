package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho lịch sử thay đổi hóa đơn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonActivityDTO {
    private Long id;
    private Long hoaDonId;
    private String maHoaDon;
    private String action;
    private String description;
    private String performedBy;
    private String performedByName;
    private LocalDateTime performedAt;
    private String oldData;
    private String newData;
    private String ipAddress;
    private String userAgent;
}

