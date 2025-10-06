package com.example.backend.dto;

public class ChatLieuVoResponse {
    private Long id;
    private String tenChatLieu;
    private String moTa;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenChatLieu() { return tenChatLieu; }
    public void setTenChatLieu(String tenChatLieu) { this.tenChatLieu = tenChatLieu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


