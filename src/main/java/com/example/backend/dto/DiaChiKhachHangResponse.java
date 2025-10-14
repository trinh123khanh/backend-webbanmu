package com.example.backend.dto;

import lombok.Data;

@Data
public class DiaChiKhachHangResponse {
    private Long id;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChi;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private Boolean macDinh;
    private Boolean trangThai;
}