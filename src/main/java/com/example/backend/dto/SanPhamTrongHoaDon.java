package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamTrongHoaDon {
    private Long id;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String ghiChu;
}
