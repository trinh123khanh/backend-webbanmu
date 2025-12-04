package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
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
    private BigDecimal tongTien;
    private BigDecimal tongGiamGia;
    private BigDecimal thanhTien;

    /**
     * Snapshot mã phiếu giảm giá và số tiền giảm đã áp dụng cho hóa đơn chờ.
     * Dùng để hiển thị lại đúng thông tin giảm giá cho từng hóa đơn, không
     * bị thay đổi khi cấu hình phiếu giảm giá được cập nhật.
     */
    private String voucherCode;
    private BigDecimal voucherDiscountAmount;
    private String voucherType;
    private BigDecimal voucherValue;
    private BigDecimal voucherMaxDiscount;
}

