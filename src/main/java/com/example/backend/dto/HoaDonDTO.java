package com.example.backend.dto;

import com.example.backend.entity.HoaDon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {
    private Long id;
    private String maHoaDon;
    private Long khachHangId;
    private String tenKhachHang;
    private String emailKhachHang;
    private String soDienThoaiKhachHang;
    private Long nhanVienId;
    private String tenNhanVien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal thanhTien;
    private String ghiChu;
    private HoaDon.TrangThaiHoaDon trangThai;
    private Integer soLuongSanPham;

    // Constructors, getters, and setters




}
