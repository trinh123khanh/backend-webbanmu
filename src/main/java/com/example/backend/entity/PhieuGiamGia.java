package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String maPhieu;
    
    @Column(nullable = false)
    private String tenPhieu;
    
    private String moTa;
    
    @Column(nullable = false)
    private LocalDateTime ngayBatDau;
    
    @Column(nullable = false)
    private LocalDateTime ngayKetThuc;
    
    @Column(nullable = false)
    private BigDecimal giaTriGiam;
    
    @Column(nullable = false)
    private BigDecimal giaTriToiThieu;
    
    @Column(nullable = false)
    private int soLuong;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoaiGiamGia loaiGiamGia;
    
    private Boolean trangThai;
    
    public enum LoaiGiamGia {
        PHAN_TRAM,
        TIEN_MAT
    }
}
