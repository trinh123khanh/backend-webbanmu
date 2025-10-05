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
    private Long hoaDonId;
    private Long chiTietSanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichThuoc;
    private int soLuong;
    private BigDecimal donGia;
    private BigDecimal giamGia;
    private BigDecimal thanhTien;
}
