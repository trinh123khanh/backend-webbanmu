package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "chi_tiet_dot_giam_gia")
public class ChiTietDotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "dot_giam_gia_id", nullable = false)
    private DotGiamGia dotGiamGia;
    
    @ManyToOne
    // Link to ChiTietSanPham (Variant) instead of SanPham (Parent)
    @JoinColumn(name = "chi_tiet_san_pham_id", nullable = false)
    private ChiTietSanPham chiTietSanPham;
    
    @ManyToOne
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;
    
    @Column(nullable = false)
    private BigDecimal phanTramGiam;
    
    private Boolean trangThai;
}
