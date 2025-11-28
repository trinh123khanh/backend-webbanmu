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
public class ShippingFeeAdjustmentRequest {
    private BigDecimal newShippingFee; // Phí ship mới
    private BigDecimal oldShippingFee; // Phí ship cũ (để tính toán chênh lệch)
    private String adjustmentType; // "REFUND" hoặc "SURCHARGE"
    private BigDecimal adjustmentAmount; // Số tiền cần hoàn hoặc tăng phụ phí
    private String reason; // Lý do điều chỉnh
    private String refundMethod; // Phương thức hoàn tiền (nếu là REFUND): "bank_transfer", "cash", "original_method"
    private String bankAccount; // Số tài khoản ngân hàng (nếu hoàn qua chuyển khoản)
    private String bankName; // Tên ngân hàng
    private String accountHolder; // Chủ tài khoản
}

