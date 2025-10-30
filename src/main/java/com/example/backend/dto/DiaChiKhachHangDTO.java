package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiKhachHangDTO {
    private Long id;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChiChiTiet;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private Boolean macDinh;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Long khachHangId;
}
