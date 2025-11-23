package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDetailDTO {
    private String period; // "Ngày 18/11", "Tuần 1", "Tháng 11", "Quý 4", etc.
    private Integer tongDonHang;
    private Integer donOffline;
    private Integer donOnline;
    private Integer donThanhCong;
    private Integer donThatBai;
    private Integer soSanPhamDaBan;
    private Integer khachHangMoi;
    private Integer khachHangQuayLai;
    private Integer luotGiamGia;
    private BigDecimal tong;
    private BigDecimal tienGiam;
    private BigDecimal thucThu;
    private BigDecimal thuThucTe;
    private BigDecimal duNo;
    private String tangTruong; // "0.0%" hoặc giá trị tăng trưởng
}

