package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelStatisticsDTO {
    private String channel;  // "Online" hoặc "Tại quầy"
    private Integer count;    // Số lượng đơn hàng
    private String color;   // Màu sắc để hiển thị
}


