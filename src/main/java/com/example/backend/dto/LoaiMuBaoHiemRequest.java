package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoaiMuBaoHiemRequest {
    @NotBlank(message = "Tên loại mũ bảo hiểm là bắt buộc")
    @Size(max = 255, message = "Tên loại mũ bảo hiểm không được vượt quá 255 ký tự")
    private String tenLoai;
    
    private String moTa;
    private Boolean trangThai;

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
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
