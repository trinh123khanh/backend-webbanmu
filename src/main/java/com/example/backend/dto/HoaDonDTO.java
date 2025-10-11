package com.example.backend.dto;

import com.example.backend.entity.HoaDon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {
    private Long id;
    private String maHoaDon;
    private Long khachHangId;
    private String tenKhachHang; // For display on frontend
    private String soDienThoaiKhachHang; // For display on frontend
    private String emailKhachHang; // For display on frontend
    private Long nhanVienId;
    private String tenNhanVien; // For display on frontend
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal thanhTien;
    private String ghiChu;
    private HoaDon.TrangThaiHoaDon trangThai;
    private String viTriBanHang; // "Tại quầy" or "Online"
    private Integer soLuongSanPham; // Total quantity of products
    private List<SanPhamTrongHoaDon> danhSachSanPham;
}
