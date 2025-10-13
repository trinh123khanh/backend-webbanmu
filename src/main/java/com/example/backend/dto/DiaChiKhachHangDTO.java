package com.example.backend.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class DiaChiKhachHangDTO {
    private Long id;
    private Long khachHangId;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChi;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private Boolean macDinh;
    private Boolean trangThai;
}
