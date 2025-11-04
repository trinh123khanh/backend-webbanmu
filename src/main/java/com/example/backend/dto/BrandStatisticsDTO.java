package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandStatisticsDTO {
    private Long nhaSanXuatId;
    private String tenNhaSanXuat;   // Tên nhà sản xuất
    private Integer tongSoLuongMua; // Tổng số lượng đã mua
}


