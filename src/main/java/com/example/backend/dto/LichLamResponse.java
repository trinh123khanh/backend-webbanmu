package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichLamResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String maNhanVien;
    private String dayOfWeek;
    private String shift;
    private String position;
    private String date;
    private Integer week;
    private Integer year;
}

