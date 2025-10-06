package com.example.backend.dto;

public class KieuDangMuResponse {
    private Long id;
    private String tenKieuDang;
    private String moTa;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenKieuDang() { return tenKieuDang; }
    public void setTenKieuDang(String tenKieuDang) { this.tenKieuDang = tenKieuDang; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


