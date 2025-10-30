package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;
    
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
    
    @Column(name = "dia_chi_chi_tiet", nullable = false, columnDefinition = "TEXT")
    private String diaChiChiTiet;
    
    @Column(name = "tinh_thanh", nullable = false)
    private String tinhThanh;
    
    @Column(name = "quan_huyen", nullable = false)
    private String quanHuyen;
    
    @Column(name = "phuong_xa", nullable = false)
    private String phuongXa;
    
    @Column(name = "mac_dinh")
    private Boolean macDinh = false;
    
    @Column(name = "trang_thai")
    private Boolean trangThai = true;
    
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    @JsonIgnore
    private KhachHang khachHang;
    
    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }
    
<<<<<<< Updated upstream
    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
=======
    @Column(nullable = false)
    private String soDienThoai;
    
    // Cho phép null để tránh lỗi khi migrate với dữ liệu cũ đang trống
    @Column(nullable = true)
    private String diaChi;
    
    @Column(nullable = false)
    private String tinhThanh;
    
    @Column(nullable = false)
    private String quanHuyen;
    
    @Column(nullable = false)
    private String phuongXa;
    
    private Boolean macDinh;
    
    private Boolean trangThai;
>>>>>>> Stashed changes
}
