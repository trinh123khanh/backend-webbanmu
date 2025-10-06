package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class NhaSanXuatRequest {
    @NotBlank
    private String ten;
    private String moTa;
    private Boolean trangThai;
    private String quocGia;

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
    public String getQuocGia() { return quocGia; }
    public void setQuocGia(String quocGia) { this.quocGia = quocGia; }
}


