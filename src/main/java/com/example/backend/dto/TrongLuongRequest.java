package com.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TrongLuongRequest {
    @NotNull(message = "Giá trị trọng lượng là bắt buộc")
    @DecimalMin(value = "0.01", message = "Giá trị trọng lượng phải lớn hơn 0")
    private BigDecimal giaTriTrongLuong;
    
    @NotBlank(message = "Đơn vị là bắt buộc")
    @Size(max = 10, message = "Đơn vị không được vượt quá 10 ký tự")
    private String donVi;
    
    private String moTa;
    
    private Boolean trangThai;

    public BigDecimal getGiaTriTrongLuong() { return giaTriTrongLuong; }
    public void setGiaTriTrongLuong(BigDecimal giaTriTrongLuong) { this.giaTriTrongLuong = giaTriTrongLuong; }
    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}
