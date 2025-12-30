package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class GioHangChoItemResponse {
    private Long id;
    private Long chiTietSanPhamId;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal giamGia;
    private BigDecimal thanhTien;
    private String mauSac;
    private String kichThuoc;
    private String anhSanPham;
}

