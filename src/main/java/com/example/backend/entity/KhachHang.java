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
    
    private String tenKhachHang;
    
    @Column(unique = true)
    private String email;
    
    private String soDienThoai;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private Integer diemTichLuy;
    private LocalDate ngayTao;
    private Boolean trangThai;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;
    
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DiaChiKhachHang> danhSachDiaChi;
}
