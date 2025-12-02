package com.example.backend.dto.chat;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long conversationId;
    private Long khachHangId;
    private Long nhanVienId;
    private String noiDung;
}


