package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String maHoaDon;
    
    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;
    
    @ManyToOne
    @JoinColumn(name = "dia_chi_giao_hang_id")
    private DiaChiKhachHang diaChiGiaoHang;
    
    @ManyToOne
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;
    
    @Column(nullable = false)
    private LocalDateTime ngayTao;
    
    private LocalDateTime ngayThanhToan;
    
    @Column(nullable = false)
    private BigDecimal tongTien;
    
    private BigDecimal tienGiamGia;
    
    @Column(nullable = false)
    private BigDecimal thanhTien;
    
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrangThaiHoaDon trangThai;
    
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoaDonChiTiet> danhSachChiTiet;
    
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhuongThucThanhToan> phuongThucThanhToan;
    
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
