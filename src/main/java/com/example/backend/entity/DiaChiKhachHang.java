package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;
    
    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;
    
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
    
    @Column(name = "dia_chi", nullable = false)
    private String diaChi;
    
    @Column(name = "tinh_thanh")
    private String tinhThanh;
    
    @Column(name = "quan_huyen")
    private String quanHuyen;
    
    @Column(name = "phuong_xa")
    private String phuongXa;
    
    @Column(name = "mac_dinh")
    private Boolean macDinh = false;
    
    @Column(name = "trang_thai")
    private Boolean trangThai = true;
}
