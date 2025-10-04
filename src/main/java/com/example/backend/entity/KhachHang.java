package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String tenKhachHang;
    
    @Column(unique = true)
    private String email;
    
    private String soDienThoai;
    private LocalDate ngaySinh;
    private boolean gioiTinh;
    private Integer diemTichLuy;
    private LocalDate ngayTao;
    private boolean trangThai;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
