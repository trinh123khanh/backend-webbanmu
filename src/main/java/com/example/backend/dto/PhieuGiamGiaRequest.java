package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaRequest {
    private String maPhieu;
    private String tenPhieuGiamGia;
    private Boolean loaiPhieuGiamGia; // false = phần trăm, true = tiền mặt
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriToiThieu;
    private BigDecimal soTienToiDa;
    private BigDecimal hoaDonToiThieu;
    private Integer soLuongDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Boolean trangThai;
    
    // Các field mới cho chế độ Công khai/Cá nhân
    private Boolean isPublic; // true = Công khai, false = Cá nhân
    private List<Long> selectedCustomerIds; // Danh sách ID khách hàng được chọn (chỉ dùng cho chế độ Cá nhân)
}
