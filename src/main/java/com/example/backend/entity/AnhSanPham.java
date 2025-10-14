package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "anh_san_pham")
public class AnhSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ten_anh")
    private String tenAnh;
    
    @Column(name = "duong_dan")
    private String duongDan;
    
    @Column(name = "trang_thai")
    private Boolean trangThai;
}
