package com.example.backend.dto;

public class XuatXuResponse {
    private Long id;
    private String tenXuatXu;
    private String moTa;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenXuatXu() { return tenXuatXu; }
    public void setTenXuatXu(String tenXuatXu) { this.tenXuatXu = tenXuatXu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}
