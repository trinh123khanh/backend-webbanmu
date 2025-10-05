package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "kieu_dang_mu")
public class KieuDangMu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String tenKieuDang;
    
    private String moTa;
    private Boolean trangThai;
}
