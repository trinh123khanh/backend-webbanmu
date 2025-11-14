package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HoaDonActivityDTO {
    private Long id;
    private Long hoaDonId;
    private String maHoaDon;
    private String action;
    private String description;
    private String performedBy;
    private String performedByName;
    private LocalDateTime performedAt;
}

