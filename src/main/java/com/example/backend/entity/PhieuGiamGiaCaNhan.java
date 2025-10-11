package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

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
    
    @Builder.Default
    @Column(name = "da_su_dung", nullable = false)
    private Boolean daSuDung = false;
    
    @Column(name = "ngay_het_han", nullable = false)
    private LocalDateTime ngayHetHan;
    
    @Column(name = "ngay_su_dung")
    private LocalDateTime ngaySuDung;
    
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
    
    // Helper methods
    public boolean isUsed() {
        return daSuDung;
    }
    
    public boolean isExpired() {
        return ngayHetHan.isBefore(LocalDateTime.now());
    }
    
    public boolean isAvailable() {
        return !daSuDung && !isExpired();
    }
    
    public void markAsUsed() {
        this.daSuDung = true;
        this.ngaySuDung = LocalDateTime.now();
    }
    
    public String getTrangThaiText() {
        if (daSuDung) {
            return "Đã sử dụng";
        } else if (isExpired()) {
            return "Hết hạn";
        } else {
            return "Có thể sử dụng";
        }
    }
}
