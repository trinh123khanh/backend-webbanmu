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
    private String tenKhachHang;
    private String emailKhachHang;
    private String soDienThoaiKhachHang;
    private String diaChiKhachHang; // Địa chỉ khách hàng từ bảng dia_chi_khach_hang
    private String tinhThanh; // Tỉnh/Thành phố
    private String quanHuyen; // Quận/Huyện
    private String phuongXa; // Xã/Phường
    private String diaChiChiTiet; // Địa chỉ chi tiết
    private Long nhanVienId;
    private String tenNhanVien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal giamGiaPhanTram; // Phần trăm giảm giá
    private BigDecimal thanhTien;
    private String ghiChu;
    private String phuongThucThanhToan; // Phương thức thanh toán
    private String trangThai; // String để map DA_HUY -> HUY cho frontend
    private Integer soLuongSanPham;
    private String viTriBanHang; // "Online" hoặc "Tại quầy" - dựa trên nhanVienId (null = Online)
    private List<HoaDonChiTietDTO> danhSachChiTiet; // Danh sách chi tiết sản phẩm trong hóa đơn

    
    // Constructors, getters, and setters




}
