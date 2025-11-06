package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HoaDonChoResponse {
    private Long id;
    private String maHoaDonCho;
    private Long khachHangId;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;
    private Long nhanVienId;
    private String tenNhanVien;
    private String ghiChu;
    private String trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private List<GioHangChoItemResponse> danhSachGioHang;
    private Long tongSoLuong;
    private java.math.BigDecimal tongTien;
    private java.math.BigDecimal tongGiamGia;
    private java.math.BigDecimal thanhTien;
}

