package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class KieuDangMuRequest {
    @NotBlank
    private String tenKieuDang;
    private String moTa;
    private Boolean trangThai;

    public String getTenKieuDang() { return tenKieuDang; }
    public void setTenKieuDang(String tenKieuDang) { this.tenKieuDang = tenKieuDang; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


