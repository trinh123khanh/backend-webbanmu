package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaResponse {
    private Long id;
    private String maPhieu;
    private String tenPhieuGiamGia;
    private Boolean loaiPhieuGiamGia;
    private String loaiPhieuGiamGiaText;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriToiThieu;
    private BigDecimal soTienToiDa;
    private BigDecimal hoaDonToiThieu;
    private Integer soLuongDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Boolean trangThai;
    private String trangThaiText;
    private Boolean isActive;
    private Boolean isExpired;
    private Boolean isNotStarted;
}
