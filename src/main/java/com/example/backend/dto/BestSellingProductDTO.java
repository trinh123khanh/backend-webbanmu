package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestSellingProductDTO {
    private Long chiTietSanPhamId;
    private Long sanPhamId;
    private String tenSanPham; // Từ bảng san_pham thông qua san_pham_id
    private String mauSac; // Từ bảng mau_sac thông qua chi_tiet_san_pham -> mau_sac_id
    private String kieuDang; // Từ bảng kieu_dang_mu thông qua san_pham -> kieu_dang_mu_id
    private BigDecimal donGia; // Từ hoa_don_chi_tiet
    private Integer soLuongBan; // Tổng số lượng đã bán từ hoa_don_chi_tiet
}

