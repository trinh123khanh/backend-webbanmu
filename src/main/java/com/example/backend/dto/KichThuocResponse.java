package com.example.backend.dto;

public class KichThuocResponse {
    private Long id;
    private String tenKichThuoc;
    private String moTa;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenKichThuoc() { return tenKichThuoc; }
    public void setTenKichThuoc(String tenKichThuoc) { this.tenKichThuoc = tenKichThuoc; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


