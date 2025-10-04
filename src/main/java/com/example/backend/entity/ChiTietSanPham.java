package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

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
    
    @Column(nullable = false)
    private Integer soLuongTon;
    
    @Column(nullable = false)
    private BigDecimal giaBan;
    
    @Column(nullable = false)
    private boolean trangThai;
}
