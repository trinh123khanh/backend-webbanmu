package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "phieu_giam_gia_ca_nhan")
public class PhieuGiamGiaCaNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "phieu_giam_gia_id", nullable = false)
    private PhieuGiamGia phieuGiamGia;
    
    @ManyToOne
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;
    
    private Boolean daSuDung;
    
    private LocalDateTime ngaySuDung;
    
    @Column(nullable = false)
    private LocalDateTime ngayHetHan;
}
