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
public class PhieuGiamGiaRequest {
    private String maPhieu;
    private String tenPhieuGiamGia;
    private Boolean loaiPhieuGiamGia; // false = phần trăm, true = tiền mặt
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriToiThieu;
    private BigDecimal soTienToiDa;
    private BigDecimal hoaDonToiThieu;
    private Integer soLuongDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Boolean trangThai;
}
