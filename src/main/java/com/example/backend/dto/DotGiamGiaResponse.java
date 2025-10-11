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
}
