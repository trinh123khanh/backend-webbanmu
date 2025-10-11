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
    
    @Column(name = "hoa_don_id", nullable = false)
    private Long hoaDonId;
    
    @Column(name = "san_pham_id", nullable = false)
    private Long sanPhamId;
    
    @Column(nullable = false)
    private int soLuong;
    
    @Column(nullable = false)
    private BigDecimal donGia;
    
    private BigDecimal giamGia;
    
    @Column(nullable = false)
    private BigDecimal thanhTien;
    
    private String ghiChu;
}
