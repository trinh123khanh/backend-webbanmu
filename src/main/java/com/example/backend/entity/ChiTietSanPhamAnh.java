package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "chi_tiet_san_pham_anh")
public class ChiTietSanPhamAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "chi_tiet_san_pham_id", nullable = false)
    private ChiTietSanPham chiTietSanPham;
    
    @ManyToOne
    @JoinColumn(name = "anh_san_pham_id", nullable = false)
    private AnhSanPham anhSanPham;
    
    private boolean anhChinh;
}
