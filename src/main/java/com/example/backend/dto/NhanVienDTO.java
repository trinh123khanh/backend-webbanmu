package com.example.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienDTO {
    private Long id;
    private String hoTen;
    private String maNhanVien;
    private String email;
    private String soDienThoai;
    private String soCanCuocCongDan;
    private String diaChi;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private LocalDate ngayVaoLam;
    private Boolean trangThai;
    
    // Thông tin User liên kết
    private Long userId;
    private String username;
    private String fullName;
}
