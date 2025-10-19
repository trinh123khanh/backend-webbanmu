package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ImeiRequest {
    
    @NotBlank(message = "Số IMEI không được để trống")
    @Pattern(regexp = "^[0-9]{15}$", message = "IMEI phải có đúng 15 chữ số")
    private String soImei;
    
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long sanPhamId;
    
    private Boolean trangThai = true;
    
    public String getSoImei() {
        return soImei;
    }
    
    public void setSoImei(String soImei) {
        this.soImei = soImei;
    }
    
    public Long getSanPhamId() {
        return sanPhamId;
    }
    
    public void setSanPhamId(Long sanPhamId) {
        this.sanPhamId = sanPhamId;
    }
    
    public Boolean getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
