package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ma_phieu", nullable = false, unique = true)
    private String maPhieu;
    
    @Column(name = "ten_phieu_giam_gia", nullable = false)
    private String tenPhieuGiamGia;
    
    @Column(name = "loai_phieu_giam_gia", nullable = false)
    private Boolean loaiPhieuGiamGia; // false = phần trăm, true = tiền mặt
    
    @Column(name = "gia_tri_giam", nullable = false, precision = 38, scale = 2)
    private BigDecimal giaTriGiam;
    
    @Column(name = "gia_tri_toi_thieu", nullable = false, precision = 38, scale = 2)
    private BigDecimal giaTriToiThieu;
    
    @Column(name = "so_tien_toi_da", nullable = false, precision = 38, scale = 2)
    private BigDecimal soTienToiDa;
    
    @Column(name = "hoa_don_toi_thieu", nullable = false, precision = 38, scale = 2)
    private BigDecimal hoaDonToiThieu;
    
    @Column(name = "so_luong_dung", nullable = false)
    private Integer soLuongDung;
    
    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;
    
    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;
    
    @Builder.Default
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = true;
    
    // Relationship với PhieuGiamGiaCaNhan
    @OneToMany(mappedBy = "phieuGiamGia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.example.backend.entity.PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans;
    
    // Helper methods
    public boolean isPhanTram() {
        return !loaiPhieuGiamGia; // false = phần trăm
    }
    
    public boolean isTienMat() {
        return loaiPhieuGiamGia; // true = tiền mặt
    }
    
    public String getLoaiPhieuGiamGiaText() {
        return loaiPhieuGiamGia ? "Tiền mặt" : "Phần trăm";
    }
    
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return trangThai && 
               ngayBatDau.isBefore(now.plusDays(1)) && 
               ngayKetThuc.isAfter(now.minusDays(1));
    }
    
    public boolean isExpired() {
        return ngayKetThuc.isBefore(LocalDate.now());
    }
    
    public boolean isNotStarted() {
        return ngayBatDau.isAfter(LocalDate.now());
    }
}
