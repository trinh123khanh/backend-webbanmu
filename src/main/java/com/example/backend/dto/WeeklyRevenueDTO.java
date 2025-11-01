package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyRevenueDTO {
    private String weekLabel;       // Ví dụ: "Tuần 1", "Tuần 2"
    private LocalDate startDate;    // Ngày bắt đầu tuần
    private LocalDate endDate;      // Ngày kết thúc tuần
    private BigDecimal totalRevenue; // Tổng doanh thu của tuần (thanhTien)
    private Integer totalOrders;     // Tổng số đơn hàng trong tuần
}

