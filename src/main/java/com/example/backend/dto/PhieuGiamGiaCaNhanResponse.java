package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaCaNhanResponse {
    private Long id;
    private Long khachHangId;
    private Long phieuGiamGiaId;
    private Boolean daSuDung;
    private LocalDateTime ngayHetHan;
    private LocalDateTime ngaySuDung;
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
