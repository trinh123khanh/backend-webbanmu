package com.example.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long khachHangId;
    private String khachHangTen;
    private String khachHangEmail;
    private Long nhanVienId;
    private String nhanVienTen;
    private String ngayTao;
    private String ngayCapNhat;
    private String trangThai; // DANG_CHO | DANG_XU_LY | DA_HOAN_THANH | DA_DONG
    private Boolean dangChoPhanHoi;
    private Boolean tuDongTraLoi;
    @Builder.Default
    private List<ChatMessageDTO> messages = new ArrayList<>();
    private Integer soTinNhanChuaDoc;
}


