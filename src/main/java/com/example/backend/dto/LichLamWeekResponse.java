package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichLamWeekResponse {
    private Integer week;
    private Integer year;
    private String weekStartDate;
    private String weekEndDate;
    private List<NhanVienCaLam> nhanVienList;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NhanVienCaLam {
        private Long userId;
        private String maNhanVien;
        private String tenNhanVien;
        private String position; // Vị trí làm việc
        private CaLamWeek caLam; // Ca làm trong tuần
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class CaLamWeek {
            private Integer thu2; // Ca làm thứ 2 (1, 2, 3, 4 hoặc null)
            private Integer thu3;
            private Integer thu4;
            private Integer thu5;
            private Integer thu6;
            private Integer thu7;
            private Integer chuNhat;
        }
    }
}

