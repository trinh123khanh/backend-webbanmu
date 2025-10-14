package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class KhachHangResponse {
    private Long id;
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private Integer diemTichLuy;
    private Integer tongSoLanMua;
    private LocalDate lanMuaGanNhat;
    private LocalDate ngayTao;
    private Boolean trangThai;
    private Long userId;
    private List<DiaChiKhachHangResponse> diaChiList;
}
