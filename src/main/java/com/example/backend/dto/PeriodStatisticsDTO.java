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
    private BigDecimal doanhThu;        // Tổng thanhTien (Thực Thu)
    private BigDecimal tongTien;        // Tổng tongTien (Tổng trước giảm giá)
    private BigDecimal tienGiamGia;     // Tổng tienGiamGia (Tiền Giảm)
    private Integer sanPhamDaBan;        // Tổng soLuongSanPham
    private Integer donHang;             // Số lượng đơn hàng
    private String period;    
    private BigDecimal actualRevenue;    // Tổng thanhTien của các hóa đơn đã thanh toán
    private BigDecimal debtRevenue;      // Công nợ = doanhThu - actualRevenue
    
    // Loại khoảng thời gian: "day", "week", "month", "year"
}

