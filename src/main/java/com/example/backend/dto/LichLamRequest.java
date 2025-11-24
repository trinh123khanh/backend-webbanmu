package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichLamRequest {
    private Long userId;
    private Integer week;
    private Integer year;
    private String position; // Vị trí làm việc
    private List<CaLamItem> caLamList; // Danh sách ca làm trong tuần
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaLamItem {
        private String dayOfWeek; // Thứ 2, Thứ 3, ...
        private Integer shift; // 1, 2, 3, 4
        private String date; // Ngày cụ thể (ví dụ: 17/11/2025)
    }
}

