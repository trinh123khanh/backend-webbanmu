package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class MauSacRequest {
    @NotBlank
    private String tenMau;
    private String maMau;
    private Boolean trangThai;

    public String getTenMau() { return tenMau; }
    public void setTenMau(String tenMau) { this.tenMau = tenMau; }
    public String getMaMau() { return maMau; }
    public void setMaMau(String maMau) { this.maMau = maMau; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


