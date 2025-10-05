package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "mau_sac")
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String tenMau;
    
    @Column(name = "ma_mau", length = 20)
    private String maMau; // Mã màu hex hoặc tên màu
    
    private Boolean trangThai;
}
