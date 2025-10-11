package com.example.backend.dto;

import lombok.Data;

@Data
public class CongNgheAnToanResponse {
    
    private Long id;
    private String tenCongNghe;
    private String moTa;
    private Boolean trangThai;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
