package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SanPhamRequest {

    @NotBlank
    private String maSanPham;

    @NotBlank
    private String tenSanPham;

    private String moTa;

    @NotNull
    private Long loaiMuBaoHiemId;

    @NotNull
    private Long nhaSanXuatId;

    @NotNull
    private Long chatLieuVoId;

    private Long trongLuongId;

    @NotNull
    private Long xuatXuId;

    @NotNull
    private Long kieuDangMuId;

    @NotNull
    private Long congNgheAnToanId;

    private Long mauSacId;

    private String anhSanPham;

    private BigDecimal giaBan;

    private Integer soLuongTon;

    private Boolean trangThai;

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public Long getLoaiMuBaoHiemId() { return loaiMuBaoHiemId; }
    public void setLoaiMuBaoHiemId(Long loaiMuBaoHiemId) { this.loaiMuBaoHiemId = loaiMuBaoHiemId; }

    public Long getNhaSanXuatId() { return nhaSanXuatId; }
    public void setNhaSanXuatId(Long nhaSanXuatId) { this.nhaSanXuatId = nhaSanXuatId; }

    public Long getChatLieuVoId() { return chatLieuVoId; }
    public void setChatLieuVoId(Long chatLieuVoId) { this.chatLieuVoId = chatLieuVoId; }

    public Long getTrongLuongId() { return trongLuongId; }
    public void setTrongLuongId(Long trongLuongId) { this.trongLuongId = trongLuongId; }

    public Long getXuatXuId() { return xuatXuId; }
    public void setXuatXuId(Long xuatXuId) { this.xuatXuId = xuatXuId; }

    public Long getKieuDangMuId() { return kieuDangMuId; }
    public void setKieuDangMuId(Long kieuDangMuId) { this.kieuDangMuId = kieuDangMuId; }

    public Long getCongNgheAnToanId() { return congNgheAnToanId; }
    public void setCongNgheAnToanId(Long congNgheAnToanId) { this.congNgheAnToanId = congNgheAnToanId; }

    public Long getMauSacId() { return mauSacId; }
    public void setMauSacId(Long mauSacId) { this.mauSacId = mauSacId; }

    public String getAnhSanPham() { return anhSanPham; }
    public void setAnhSanPham(String anhSanPham) { this.anhSanPham = anhSanPham; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}


