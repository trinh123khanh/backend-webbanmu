package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class HoaDonChoRequest {
    private String maHoaDonCho;
    private Long khachHangId;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;
    private Long nhanVienId;
    private String tenNhanVien;
    private String ghiChu;
    private String trangThai;
    private List<GioHangChoItemRequest> danhSachGioHang;

    /**
     * Snapshot mã phiếu giảm giá và số tiền giảm giá được áp dụng cho hóa đơn chờ.
     * Nếu null thì coi như hóa đơn chờ chưa áp dụng phiếu giảm giá.
     */
    private String voucherCode;
    private BigDecimal voucherDiscountAmount;
    private String voucherType;
    private BigDecimal voucherValue;
    private BigDecimal voucherMaxDiscount;
}

