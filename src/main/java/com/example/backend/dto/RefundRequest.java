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
public class RefundRequest {
    private BigDecimal refundAmount; // Số tiền cần hoàn
    private String refundReason; // Lý do hoàn tiền
    private String refundMethod; // Phương thức hoàn tiền: "bank_transfer", "cash", "original_method"
    private String bankAccount; // Số tài khoản ngân hàng (nếu hoàn qua chuyển khoản)
    private String bankName; // Tên ngân hàng
    private String accountHolder; // Chủ tài khoản
}

