package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "hoa_don_cho")
public class HoaDonCho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String maHoaDonCho;
    
    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;
    
    @Column(name = "ten_khach_hang")
    private String tenKhachHang;
    
    @Column(name = "so_dien_thoai_khach_hang")
    private String soDienThoaiKhachHang;
    
    @ManyToOne
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;
    
    @Column(name = "ten_nhan_vien")
    private String tenNhanVien;
    
    @Column(nullable = false)
    private LocalDateTime ngayTao;
    
    @Column(nullable = false)
    private LocalDateTime ngayCapNhat;
    
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    @Column(nullable = false)
    private String trangThai = "DANG_CHO";
    
    @OneToMany(mappedBy = "hoaDonCho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GioHangCho> gioHangCho;

    /**
     * Snapshot mã phiếu giảm giá được áp dụng cho hóa đơn chờ tại thời điểm lưu.
     * Mục tiêu: khi cấu hình phiếu giảm giá thay đổi, hóa đơn chờ cũ vẫn giữ
     * đúng mã và số tiền giảm như lúc tạo, không tính lại theo cấu hình mới.
     */
    @Column(name = "voucher_code")
    private String voucherCode;

    /**
     * Số tiền giảm giá (VNĐ) đã được áp dụng cho hóa đơn chờ này từ phiếu giảm giá.
     * Đây là giá trị đã được tính sẵn tại thời điểm áp dụng voucher, không phụ thuộc
     * vào cấu hình hiện tại của phiếu giảm giá.
     */
    @Column(name = "voucher_discount_amount", precision = 38, scale = 2)
    private BigDecimal voucherDiscountAmount;

    /**
     * Snapshot loại voucher và giá trị cấu hình tại thời điểm áp dụng.
     * Giúp hiển thị lại đúng % hoặc số tiền giảm trên UI dù cấu hình master đã thay đổi.
     */
    @Column(name = "voucher_type")
    private String voucherType; // "PERCENT" hoặc "FIXED"

    @Column(name = "voucher_value", precision = 38, scale = 2)
    private BigDecimal voucherValue;

    @Column(name = "voucher_max_discount", precision = 38, scale = 2)
    private BigDecimal voucherMaxDiscount;
    
    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}

