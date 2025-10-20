package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String hoTen;
    
    @Column(unique = true, nullable = false)
    private String maNhanVien;
    
    @Column(unique = true)
    private String email;
    
    private String soDienThoai;
    private String diaChi;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private LocalDate ngayVaoLam;
    private Boolean trangThai;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
