package com.example.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private String loaiNguoiGui; // KHACH_HANG | NHAN_VIEN | CHATBOT
    private Long khachHangId;
    private String khachHangTen;
    private Long nhanVienId;
    private String nhanVienTen;
    private String noiDung;
    private String thoiGianGui;
    private Boolean tuDongTraLoi;
    private Boolean daDoc;
}

