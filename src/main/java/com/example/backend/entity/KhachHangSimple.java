package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "khach_hang")
public class KhachHangSimple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ten_khach_hang")
    private String tenKhachHang;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
}