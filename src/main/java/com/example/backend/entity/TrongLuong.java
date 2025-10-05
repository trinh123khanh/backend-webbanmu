package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "trong_luong")
public class TrongLuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal giaTriTrongLuong;
    
    @Column(length = 10)
    private String donVi; // gram, kg
    
    private String moTa;
    
    private Boolean trangThai;
}
