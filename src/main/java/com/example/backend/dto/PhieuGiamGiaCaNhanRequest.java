package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaCaNhanRequest {
    private Long id;
    private Long khachHangId;
    private Long phieuGiamGiaId;
    private Boolean daSuDung;
    private LocalDateTime ngayHetHan;
    private LocalDateTime ngaySuDung;
}
