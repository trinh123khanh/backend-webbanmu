package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusStatisticsDTO {
    private String label;      // Tên trạng thái: "Chờ xác nhận", "Chờ giao hàng", etc.
    private Integer count;      // Số lượng đơn hàng
    private String color;      // Màu sắc để hiển thị
    private String statusCode; // Mã trạng thái từ enum
}


