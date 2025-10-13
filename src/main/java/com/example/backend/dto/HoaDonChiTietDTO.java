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
public class HoaDonChiTietDTO {
    private Long id;
    private Long chiTietSanPhamId;
    private String tenSanPham;
    private String maSanPham;
    private String mauSac;
    private String kichThuoc;
    private String nhaSanXuat;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal giamGia;
    private BigDecimal thanhTien;
    private String anhSanPham;
}