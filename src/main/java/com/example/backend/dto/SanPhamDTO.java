package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamDTO {
    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private BigDecimal giaBan;
    private Integer soLuongTon;
    private String danhMuc;
    private String thuongHieu;
    private String xuatXu;
    private LocalDate ngayTao;
    private LocalDate ngayCapNhat;
    private Boolean trangThai;
    private String hinhAnh;
}
