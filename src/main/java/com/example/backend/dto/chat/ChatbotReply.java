package com.example.backend.dto.chat;

import com.example.backend.dto.SanPhamResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotReply {
    private String replyText;
    private List<SanPhamResponse> suggestedProducts;
}

