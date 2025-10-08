package com.example.backend.dto;

import java.math.BigDecimal;

public class TrongLuongResponse {
    private Long id;
    private BigDecimal giaTriTrongLuong;
    private String donVi;
    private String moTa;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getGiaTriTrongLuong() { return giaTriTrongLuong; }
    public void setGiaTriTrongLuong(BigDecimal giaTriTrongLuong) { this.giaTriTrongLuong = giaTriTrongLuong; }
    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}
