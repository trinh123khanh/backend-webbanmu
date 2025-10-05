package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "san_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String maSanPham;
    
    @Column(nullable = false)
    private String tenSanPham;
    
    @Column(columnDefinition = "TEXT")
    private String moTa;
    
    @ManyToOne
    @JoinColumn(name = "loai_mu_bao_hiem_id")
    private LoaiMuBaoHiem loaiMuBaoHiem;
    
    @ManyToOne
    @JoinColumn(name = "nha_san_xuat_id")
    private NhaSanXuat nhaSanXuat;
    
    @ManyToOne
    @JoinColumn(name = "chat_lieu_vo_id")
    private ChatLieuVo chatLieuVo;
    
    @ManyToOne
    @JoinColumn(name = "trong_luong_id")
    private TrongLuong trongLuong;
    
    @ManyToOne
    @JoinColumn(name = "xuat_xu_id")
    private XuatXu xuatXu;
    
    @ManyToOne
    @JoinColumn(name = "kieu_dang_mu_id")
    private KieuDangMu kieuDangMu;
    
    @ManyToOne
    @JoinColumn(name = "cong_nghe_an_toan_id")
    private CongNgheAnToan congNgheAnToan;
    
    @Column(nullable = false)
    private BigDecimal giaBan;
    
    private LocalDate ngayTao;
    
    private Boolean trangThai;
}
