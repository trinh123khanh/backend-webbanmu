package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.math.BigDecimal;

public class ChiTietSanPhamRequest {
    private Long sanPhamId;
    private Long kichThuocId;
    private Long mauSacId;
    private Long trongLuongId;
    private String trongLuongTen; // Giá trị nhập tay khi trongLuongId null
    // Nhận từ FE dưới dạng chuỗi để linh hoạt nhập liệu
    private String giaBan;
    private String soLuongTon;
    private Boolean trangThai;
    private String anhSanPham;

    public Long getSanPhamId() { return sanPhamId; }
    public void setSanPhamId(Long sanPhamId) { this.sanPhamId = sanPhamId; }
    @JsonSetter("sanPhamId")
    public void setSanPhamIdFromAny(Object v) { this.sanPhamId = toLong(v); }

    public Long getKichThuocId() { return kichThuocId; }
    public void setKichThuocId(Long kichThuocId) { this.kichThuocId = kichThuocId; }
    @JsonSetter("kichThuocId")
    public void setKichThuocIdFromAny(Object v) { this.kichThuocId = toLong(v); }

    public Long getMauSacId() { return mauSacId; }
    public void setMauSacId(Long mauSacId) { this.mauSacId = mauSacId; }
    @JsonSetter("mauSacId")
    public void setMauSacIdFromAny(Object v) { this.mauSacId = toLong(v); }

    public Long getTrongLuongId() { return trongLuongId; }
    public void setTrongLuongId(Long trongLuongId) { this.trongLuongId = trongLuongId; }
    @JsonSetter("trongLuongId")
    public void setTrongLuongIdFromAny(Object v) { this.trongLuongId = toLong(v); }

    public String getTrongLuongTen() { return trongLuongTen; }
    public void setTrongLuongTen(String trongLuongTen) { this.trongLuongTen = trongLuongTen; }
    @JsonSetter("trongLuongTen")
    public void setTrongLuongTenFromAny(Object v) { this.trongLuongTen = v != null ? v.toString().trim() : null; }

    public String getGiaBan() { return giaBan; }
    public void setGiaBan(String giaBan) { this.giaBan = giaBan; }
    @JsonSetter("giaBan")
    public void setGiaBanFromAny(Object v) { this.giaBan = v != null ? v.toString() : null; }

    public String getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(String soLuongTon) { this.soLuongTon = soLuongTon; }
    @JsonSetter("soLuongTon")
    public void setSoLuongTonFromAny(Object v) { this.soLuongTon = v != null ? v.toString() : null; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    public String getAnhSanPham() { return anhSanPham; }
    public void setAnhSanPham(String anhSanPham) { this.anhSanPham = anhSanPham; }
    @JsonSetter("anhSanPham")
    public void setAnhSanPhamFromAny(Object v) { this.anhSanPham = v != null ? v.toString() : null; }

    // Helpers chuyển đổi an toàn từ Number/String sang kiểu mong muốn
    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString().trim()); } catch (Exception e) { return null; }
    }

    // Vẫn giữ helper parse khi cần dùng từ nơi khác
    public static Integer parseIntegerSafe(String s) {
        if (s == null) return null;
        try {
            String cleaned = s.trim().replace(" ", "").replace(",", "");
            // loại bỏ mọi ký tự không phải số
            cleaned = cleaned.replaceAll("[^0-9-]", "");
            return cleaned.isEmpty() ? null : Integer.parseInt(cleaned);
        } catch (Exception e) { return null; }
    }
    public static BigDecimal parseBigDecimalSafe(String s) {
        if (s == null) return null;
        try {
            String cleaned = s.trim().replace(" ", "").replace(",", "");
            int firstDot = cleaned.indexOf('.');
            int lastDot = cleaned.lastIndexOf('.');
            if (firstDot != -1 && lastDot != -1 && firstDot != lastDot) cleaned = cleaned.replace(".", "");
            return cleaned.isEmpty() ? null : new BigDecimal(cleaned);
        } catch (Exception e) { return null; }
    }
}
