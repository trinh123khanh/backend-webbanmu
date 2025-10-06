package com.example.backend.dto;

public class MauSacResponse {
    private Long id;
    private String tenMau;
    private String maMau;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenMau() { return tenMau; }
    public void setTenMau(String tenMau) { this.tenMau = tenMau; }
    public String getMaMau() { return maMau; }
    public void setMaMau(String maMau) { this.maMau = maMau; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


