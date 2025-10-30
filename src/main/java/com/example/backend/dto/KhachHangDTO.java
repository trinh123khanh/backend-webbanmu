package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangDTO {
    private Long id;
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private LocalDate ngayTao;
    private Boolean trangThai;

    private Integer soLanMua;
    private Integer diemTichLuy;
    private LocalDate lanMuaGanNhat;

    private Long userId;
    private String username;
    
    // Địa chỉ mặc định từ bảng dia_chi_khach_hang
    private Boolean coDiaChiMacDinh;
    private String diaChiMacDinh;
    private String phuongXaMacDinh;
    private String quanHuyenMacDinh;
    private String tinhThanhMacDinh;
}

