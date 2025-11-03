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
public class PeriodStatisticsDTO {
    private BigDecimal doanhThu;        // Tổng thanhTien
    private Integer sanPhamDaBan;        // Tổng soLuongSanPham
    private Integer donHang;             // Số lượng đơn hàng
    private String period;    
    
    // Loại khoảng thời gian: "day", "week", "month", "year"
}

