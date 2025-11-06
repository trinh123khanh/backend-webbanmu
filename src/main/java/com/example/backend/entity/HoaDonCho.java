package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
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

