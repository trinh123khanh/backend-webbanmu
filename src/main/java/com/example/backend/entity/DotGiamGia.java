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


    @Column(name = "ma_dot_giam_gia", nullable = false, unique = true)
    private String maDotGiamGia;
    
    @Column(name = "loai_dot_giam_gia")
    private String loaiDotGiamGia;
    
    @Column(name = "gia_tri_dot_giam")
    private String giaTriDotGiam;
    
    @Column(name = "so_tien")
    private Long soTien;
    
    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;
    
    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;
    
    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;
    
    @Column(name = "so_luong_su_dung", nullable = false)
    private Integer soLuongSuDung;
    
    @Column(name = "ten_dot_giam_gia", nullable = false)
    private String tenDotGiamGia;
    
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai;
}
