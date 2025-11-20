package com.example.backend.dto;


public class ChiTietSanPhamResponse {
    private Long id;
    private Long sanPhamId;
    private String sanPhamTen;
    private Long kichThuocId;
    private String kichThuocTen;
    private Long mauSacId;
    private String mauSacTen;
    private String mauSacMa;
    private Long trongLuongId;
    private String trongLuongTen;
    private String giaBan;
    private String soLuongTon;
    private Boolean trangThai;
    private String anhSanPham;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSanPhamId() { return sanPhamId; }
    public void setSanPhamId(Long sanPhamId) { this.sanPhamId = sanPhamId; }
    public String getSanPhamTen() { return sanPhamTen; }
    public void setSanPhamTen(String sanPhamTen) { this.sanPhamTen = sanPhamTen; }
    public Long getKichThuocId() { return kichThuocId; }
    public void setKichThuocId(Long kichThuocId) { this.kichThuocId = kichThuocId; }
    public String getKichThuocTen() { return kichThuocTen; }
    public void setKichThuocTen(String kichThuocTen) { this.kichThuocTen = kichThuocTen; }
    public Long getMauSacId() { return mauSacId; }
    public void setMauSacId(Long mauSacId) { this.mauSacId = mauSacId; }
    public String getMauSacTen() { return mauSacTen; }
    public void setMauSacTen(String mauSacTen) { this.mauSacTen = mauSacTen; }
    public String getMauSacMa() { return mauSacMa; }
    public void setMauSacMa(String mauSacMa) { this.mauSacMa = mauSacMa; }
    public Long getTrongLuongId() { return trongLuongId; }
    public void setTrongLuongId(Long trongLuongId) { this.trongLuongId = trongLuongId; }
    public String getTrongLuongTen() { return trongLuongTen; }
    public void setTrongLuongTen(String trongLuongTen) { this.trongLuongTen = trongLuongTen; }
    public String getGiaBan() { return giaBan; }
    public void setGiaBan(String giaBan) { this.giaBan = giaBan; }
    public String getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(String soLuongTon) { this.soLuongTon = soLuongTon; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
    public String getAnhSanPham() { return anhSanPham; }
    public void setAnhSanPham(String anhSanPham) { this.anhSanPham = anhSanPham; }
}
