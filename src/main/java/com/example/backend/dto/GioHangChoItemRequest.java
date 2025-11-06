package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class GioHangChoItemRequest {
    private Long chiTietSanPhamId;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal giamGia;
    private BigDecimal thanhTien;
}

