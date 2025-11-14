package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hoa_don_activity")
public class HoaDonActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hoa_don_id")
    private Long hoaDonId;

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "performed_by_name", length = 150)
    private String performedByName;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;
}

