package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gio_hang_cho")
public class GioHangCho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "hoa_don_cho_id", nullable = false)
    private HoaDonCho hoaDonCho;
    
    @ManyToOne
    @JoinColumn(name = "chi_tiet_san_pham_id", nullable = false)
    private ChiTietSanPham chiTietSanPham;
    
    @Column(name = "ten_san_pham")
    private String tenSanPham;
    
    @Column(nullable = false)
    private Integer soLuong = 1;
    
    @Column(nullable = false)
    private BigDecimal donGia;
    
    @Column
    private BigDecimal giamGia = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private BigDecimal thanhTien;
    
    @Column(nullable = false)
    private LocalDateTime ngayTao;
    
    @Column(nullable = false)
    private LocalDateTime ngayCapNhat;
    
    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (thanhTien == null) {
            calculateThanhTien();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
        calculateThanhTien();
    }
    
    private void calculateThanhTien() {
        if (donGia != null && soLuong != null) {
            BigDecimal total = donGia.multiply(BigDecimal.valueOf(soLuong));
            if (giamGia != null) {
                total = total.subtract(giamGia);
            }
            thanhTien = total.max(BigDecimal.ZERO);
        }
    }
}

