package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DotGiamGiaResponse {
    private Long id;
    private String maDotGiamGia;
    private String loaiDotGiamGia;
    private String giaTriDotGiam;
    private Long soTien;
    private String moTa;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer soLuongSuDung;
    private String tenDotGiamGia;
    private Boolean trangThai;
    
    private java.util.List<ChiTietDotGiamGiaResponse> chiTietDotGiamGias;
    
    @Data
    public static class ChiTietDotGiamGiaResponse {
        private Long id;
        private Long chiTietSanPhamId;
        private String tenSanPham;
        private String mauSac;
        private String kichThuoc;
        private java.math.BigDecimal phanTramGiam;
        private java.math.BigDecimal giaBan;
        private java.math.BigDecimal giaSauGiam;
    }
}
