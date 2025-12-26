package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedStatisticsDTO {
    // Tổng số đơn hàng trong khoảng thời gian
    private Integer tongDonHang;
    
    // Đơn offline: đơn hàng có mã nhân viên
    private Integer donOffline;
    
    // Đơn online: đơn hàng không có mã nhân viên
    private Integer donOnline;
    
    // Đơn thành công: đơn có trạng thái DA_GIAO_HANG
    private Integer donThanhCong;
    
    // Đơn thất bại: đơn có trạng thái DA_HUY
    private Integer donThatBai;
    
    // Tổng số sản phẩm đã bán
    private Integer soSanPhamDaBan;
    
    // Khách hàng mới: khách hàng được thêm vào DB trong khoảng thời gian đó
    private Integer khachHangMoi;
    
    // Khách hàng quay lại: khách hàng đã thêm trước khoảng thời gian đó và giờ mua thêm
    private Integer khachHangQuayLai;
    
    // Tổng số khách hàng của các hóa đơn (không trùng lặp)
    private Integer tongSoKhachHang;
    
    // Lượt giảm giá: tổng lượt giảm giá đã dùng (số hóa đơn có tienGiamGia > 0)
    private Integer luotGiamGia;
    
    // Tổng: tổng các tổng tiền (tongTien) của các hóa đơn
    private BigDecimal tong;
    
    // Tiền giảm: tổng tiền giảm giá (tienGiamGia) của các hóa đơn
    private BigDecimal tienGiam;
    
    // Thực thu: tổng thành tiền (thanhTien) của TẤT CẢ các hóa đơn (không phân biệt trạng thái)
    private BigDecimal thucThu;
    
    // Thu Thực tế: tổng thành tiền (thanhTien) của các hóa đơn có trạng thái DA_GIAO_HANG (đã hoàn thành)
    private BigDecimal thuThucTe;
    
    // Dư nợ: tổng thành tiền (thanhTien) của các hóa đơn có trạng thái CHO_XAC_NHAN, CHO_VAN_CHUYEN, DANG_GIAO_HANG
    private BigDecimal duNo;
    
    // Dữ liệu chi tiết theo từng period (ngày/tuần/tháng/quý/năm)
    private List<PeriodDetailDTO> chiTietTheoPeriod;
}

