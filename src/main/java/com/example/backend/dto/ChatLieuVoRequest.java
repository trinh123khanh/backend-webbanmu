package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatLieuVoRequest {
    @NotBlank
    private String tenChatLieu;
    private String moTa;
    private Boolean trangThai;

    public String getTenChatLieu() { return tenChatLieu; }
    public void setTenChatLieu(String tenChatLieu) { this.tenChatLieu = tenChatLieu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


