package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class KichThuocRequest {
    @NotBlank
    private String tenKichThuoc;
    private String moTa;
    private Boolean trangThai;

    public String getTenKichThuoc() { return tenKichThuoc; }
    public void setTenKichThuoc(String tenKichThuoc) { this.tenKichThuoc = tenKichThuoc; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


