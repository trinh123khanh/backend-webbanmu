package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "phieu_giam_gia_ca_nhan")
public class PhieuGiamGiaCaNhan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "khach_hang_id", nullable = false)
    private Long khachHangId;
    
    @Column(name = "phieu_giam_gia_id", nullable = false)
    private Long phieuGiamGiaId;
    
    // Relationship với PhieuGiamGia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_giam_gia_id", insertable = false, updatable = false)
    private PhieuGiamGia phieuGiamGia;
    
    // Relationship với KhachHang (nếu có entity KhachHang)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "khach_hang_id", insertable = false, updatable = false)
    // private KhachHang khachHang;
    
}
