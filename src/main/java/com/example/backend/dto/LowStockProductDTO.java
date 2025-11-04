package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductDTO {
    private Long sanPhamId;        // ID sản phẩm
    private String tenSanPham;     // Tên sản phẩm
    private Integer soLuongTon;    // Số lượng tồn kho hiện tại
}

