package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ma_khach_hang")
    private String maKhachHang;
    
    @Column(name = "ten_khach_hang", nullable = false)
    private String tenKhachHang;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
    
    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;
    
    @Column(name = "gioi_tinh", nullable = false)
    private Boolean gioiTinh;
    
    @Column(name = "diem_tich_luy")
    private Integer diemTichLuy;
    
    @Column(name = "tong_so_lan_mua")
    private Integer tongSoLanMua;
    
    @Column(name = "lan_mua_gan_nhat")
    private LocalDate lanMuaGanNhat;
    
    @Column(name = "ngay_tao")
    private LocalDate ngayTao;
    
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai;
    
    @Column(name = "user_id")
    private Long userId;
    
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaChiKhachHang> diaChiList;
    
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoaDon> hoaDonList;
    
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhanList;
}
