package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaCaNhanResponse {
    private Long id;
    private Long khachHangId;
    private Long phieuGiamGiaId;
    private String trangThai;
    private Integer soLanDaDung;
    
    // Thông tin liên quan
    private String tenKhachHang;
    private String tenPhieuGiamGia;
    private String maPhieuGiamGia;
    private Double giaTriGiam;
    private Boolean loaiPhieuGiamGia;
    private String loaiPhieuGiamGiaText;
}
