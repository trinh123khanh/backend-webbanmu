package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "khach_hang_id", nullable = false)
    @JsonIgnore
    private KhachHang khachHang;
    
    @Column(nullable = false)
    private String tenNguoiNhan;
    
    @Column(nullable = false)
    private String soDienThoai;
    
    @Column(nullable = false)
    private String diaChi;
    
    @Column(nullable = false)
    private String tinhThanh;
    
    @Column(nullable = false)
    private String quanHuyen;
    
    @Column(nullable = false)
    private String phuongXa;
    
    private Boolean macDinh;
    
    private Boolean trangThai;
}
