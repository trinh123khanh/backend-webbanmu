package com.example.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private Integer diemTichLuy;
    private LocalDate ngayTao;
    private Boolean trangThai;
    private Integer soLanMua;
    private LocalDate lanMuaGanNhat;
    private Long userId;
    private String username;
    private String fullName;
    private List<DiaChiKhachHangDTO> danhSachDiaChi;
}