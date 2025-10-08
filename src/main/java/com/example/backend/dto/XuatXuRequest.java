package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class XuatXuRequest {
    @NotBlank
    private String tenXuatXu;
    private String moTa;
    private Boolean trangThai;

    public String getTenXuatXu() { return tenXuatXu; }
    public void setTenXuatXu(String tenXuatXu) { this.tenXuatXu = tenXuatXu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}



