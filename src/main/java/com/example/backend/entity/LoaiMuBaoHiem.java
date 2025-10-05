package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "loai_mu_bao_hiem")
public class LoaiMuBaoHiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String tenLoai;
    
    private String moTa;
    private Boolean trangThai;
}
