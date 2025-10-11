package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CongNgheAnToanRequest {
    
    @NotBlank(message = "Tên công nghệ an toàn không được để trống")
    private String tenCongNghe;
    
    private String moTa;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Boolean trangThai;
    
    // Getters and Setters
    public String getTenCongNghe() {
        return tenCongNghe;
    }
    
    public void setTenCongNghe(String tenCongNghe) {
        this.tenCongNghe = tenCongNghe;
    }
    
    public String getMoTa() {
        return moTa;
    }
    
    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
    
    public Boolean getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
