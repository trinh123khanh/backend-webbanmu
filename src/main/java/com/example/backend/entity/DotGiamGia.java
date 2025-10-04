package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String maDotGiamGia;
    
    @Column(nullable = false)
    private String tenDotGiamGia;
    
    @Column(columnDefinition = "TEXT")
    private String moTa;
    
    @Column(nullable = false)
    private LocalDateTime ngayBatDau;
    
    @Column(nullable = false)
    private LocalDateTime ngayKetThuc;
    
    @Column(nullable = false)
    private int soLuongSuDung;
    
    @Column(nullable = false)
    private boolean trangThai;
}
