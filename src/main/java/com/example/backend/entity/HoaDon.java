package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ma_hoa_don", nullable = false, unique = true)
    private String maHoaDon;
    
    @Column(name = "khach_hang_id")
    private Long khachHangId;
    
    @Column(name = "nhan_vien_id")
    private Long nhanVienId;
    
    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
    
    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;
    
    @Column(name = "tong_tien", nullable = false, precision = 38, scale = 2)
    private BigDecimal tongTien;
    
    @Column(name = "tien_giam_gia", precision = 38, scale = 2)
    private BigDecimal tienGiamGia;
    
    @Column(name = "thanh_tien", nullable = false, precision = 38, scale = 2)
    private BigDecimal thanhTien;
    
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiHoaDon trangThai;
    
    public enum TrangThaiHoaDon {
        CHO_XAC_NHAN,
        DA_XAC_NHAN,
        DANG_GIAO_HANG,
        DA_GIAO_HANG,
        DA_HUY,
        TRA_HANG,
        HOAN_TIEN
    }
}
