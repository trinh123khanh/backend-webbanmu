package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;
    
    @ManyToOne
    @JoinColumn(name = "hinh_thuc_thanh_toan_id", nullable = false)
    private HinhThucThanhToan hinhThucThanhToan;
    
    @Column(nullable = false)
    private BigDecimal soTienThanhToan;
    
    private String maGiaoDich;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrangThaiThanhToan trangThai;
    
    private String ghiChu;
    
    public enum TrangThaiThanhToan {
        CHO_THANH_TOAN,
        DA_THANH_TOAN,
        DA_HOAN_TIEN,
        THAT_BAI
    }
}
