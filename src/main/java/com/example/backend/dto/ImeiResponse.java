package com.example.backend.dto;

import java.time.LocalDateTime;

public class ImeiResponse {
    private Long id;
    private String soImei;
    private Long sanPhamId;
    private String sanPhamTen;
    private String sanPhamMa;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getSanPhamTen() {
        return sanPhamTen;
    }
    
    public void setSanPhamTen(String sanPhamTen) {
        this.sanPhamTen = sanPhamTen;
    }
    
    public String getSanPhamMa() {
        return sanPhamMa;
    }
    
    public void setSanPhamMa(String sanPhamMa) {
        this.sanPhamMa = sanPhamMa;
    }
    
    public Boolean getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
    
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }
    
    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
    
    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }
    
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
