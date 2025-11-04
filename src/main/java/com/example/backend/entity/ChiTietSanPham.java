package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;
    
    @ManyToOne
    @JoinColumn(name = "mau_sac_id")
    private MauSac mauSac;
    
    @ManyToOne
    @JoinColumn(name = "kich_thuoc_id")
    private KichThuoc kichThuoc;
    
    @Column(name = "trong_luong_ten")
    private String trongLuongTen; // Lưu giá trị nhập tay khi trongLuongId null
    
    @Column(nullable = false)
    private String soLuongTon;
    
    @Column(nullable = false)
    private String giaBan;
    
    private Boolean trangThai;
}
