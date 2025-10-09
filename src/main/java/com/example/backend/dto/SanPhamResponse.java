package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SanPhamResponse {
    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private Long loaiMuBaoHiemId;
    private String loaiMuBaoHiemTen;
    private Long nhaSanXuatId;
    private String nhaSanXuatTen;
    private Long chatLieuVoId;
    private String chatLieuVoTen;
    private Long trongLuongId;
    private String trongLuongTen;
    private Long xuatXuId;
    private String xuatXuTen;
    private Long kieuDangMuId;
    private String kieuDangMuTen;
    private Long congNgheAnToanId;
    private String congNgheAnToanTen;
    private BigDecimal giaBan;
    private LocalDate ngayTao;
    private Boolean trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Long getLoaiMuBaoHiemId() { return loaiMuBaoHiemId; }
    public void setLoaiMuBaoHiemId(Long loaiMuBaoHiemId) { this.loaiMuBaoHiemId = loaiMuBaoHiemId; }
    public String getLoaiMuBaoHiemTen() { return loaiMuBaoHiemTen; }
    public void setLoaiMuBaoHiemTen(String loaiMuBaoHiemTen) { this.loaiMuBaoHiemTen = loaiMuBaoHiemTen; }
    public Long getNhaSanXuatId() { return nhaSanXuatId; }
    public void setNhaSanXuatId(Long nhaSanXuatId) { this.nhaSanXuatId = nhaSanXuatId; }
    public String getNhaSanXuatTen() { return nhaSanXuatTen; }
    public void setNhaSanXuatTen(String nhaSanXuatTen) { this.nhaSanXuatTen = nhaSanXuatTen; }
    public Long getChatLieuVoId() { return chatLieuVoId; }
    public void setChatLieuVoId(Long chatLieuVoId) { this.chatLieuVoId = chatLieuVoId; }
    public String getChatLieuVoTen() { return chatLieuVoTen; }
    public void setChatLieuVoTen(String chatLieuVoTen) { this.chatLieuVoTen = chatLieuVoTen; }
    public Long getTrongLuongId() { return trongLuongId; }
    public void setTrongLuongId(Long trongLuongId) { this.trongLuongId = trongLuongId; }
    public String getTrongLuongTen() { return trongLuongTen; }
    public void setTrongLuongTen(String trongLuongTen) { this.trongLuongTen = trongLuongTen; }
    public Long getXuatXuId() { return xuatXuId; }
    public void setXuatXuId(Long xuatXuId) { this.xuatXuId = xuatXuId; }
    public String getXuatXuTen() { return xuatXuTen; }
    public void setXuatXuTen(String xuatXuTen) { this.xuatXuTen = xuatXuTen; }
    public Long getKieuDangMuId() { return kieuDangMuId; }
    public void setKieuDangMuId(Long kieuDangMuId) { this.kieuDangMuId = kieuDangMuId; }
    public String getKieuDangMuTen() { return kieuDangMuTen; }
    public void setKieuDangMuTen(String kieuDangMuTen) { this.kieuDangMuTen = kieuDangMuTen; }
    public Long getCongNgheAnToanId() { return congNgheAnToanId; }
    public void setCongNgheAnToanId(Long congNgheAnToanId) { this.congNgheAnToanId = congNgheAnToanId; }
    public String getCongNgheAnToanTen() { return congNgheAnToanTen; }
    public void setCongNgheAnToanTen(String congNgheAnToanTen) { this.congNgheAnToanTen = congNgheAnToanTen; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


