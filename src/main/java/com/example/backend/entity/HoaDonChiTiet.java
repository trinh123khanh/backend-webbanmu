package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "hoa_don_chi_tiet")
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;
    
    @ManyToOne
    @JoinColumn(name = "chi_tiet_san_pham_id", nullable = false)
    private ChiTietSanPham chiTietSanPham;
    
    @Column(nullable = false)
    private int soLuong;
    
    @Column(nullable = false)
    private BigDecimal donGia;
    
    private BigDecimal giamGia;
    
    @Column(nullable = false)
    private BigDecimal thanhTien;
}
