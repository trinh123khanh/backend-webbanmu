package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   // Bổ sung mã khách hàng cho khớp với cột "ma_khach_hang"
    @Column(name = "ma_khach_hang")
    private String maKhachHang;

    @Column(name = "ten_khach_hang")
    
    @Column(unique = true, nullable = false)
    private String maKhachHang;
 
    private String tenKhachHang;

    @Column(unique = true)
    private String email;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "ngay_sinh")

    private String diaChi;

    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "diem_tich_luy")
    private Integer diemTichLuy;

    @Column(name = "ngay_tao")

    private LocalDate ngayTao;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "so_lan_mua")
    private Integer soLanMua;

    @Column(name = "lan_mua_gan_nhat")
    private LocalDate lanMuaGanNhat;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DiaChiKhachHang> danhSachDiaChi;
}
