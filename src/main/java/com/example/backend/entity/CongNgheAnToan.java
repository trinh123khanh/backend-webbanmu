package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cong_nghe_an_toan")
public class CongNgheAnToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String tenCongNghe;
    
    @Column(columnDefinition = "TEXT")
    private String moTa;
    
    private Boolean trangThai;
}
