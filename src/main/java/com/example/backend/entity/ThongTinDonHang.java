package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "thong_tin_don_hang")
public class ThongTinDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "hoa_don_id", nullable = false, unique = true)
    private HoaDon hoaDon;
    
    @Column(nullable = false)
    private String tenNguoiNhan;
    
    @Column(nullable = false)
    private String soDienThoai;
    
    @Column(nullable = false)
    private String diaChiGiaoHang;
    
    @Column(nullable = false)
    private String tinhThanh;
    
    @Column(nullable = false)
    private String quanHuyen;
    
    @Column(nullable = false)
    private String phuongXa;
    
    private BigDecimal phiVanChuyen;
    
    private LocalDateTime ngayGiaoHangDuKien;
    
    private LocalDateTime ngayGiaoHangThucTe;
    
    // Thông tin vận chuyển chi tiết
    private BigDecimal khoiLuong; // Khối lượng (kg)
    private BigDecimal chieuDai; // Chiều dài (cm)
    private BigDecimal chieuRong; // Chiều rộng (cm)
    private BigDecimal chieuCao; // Chiều cao (cm)
    
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
}
