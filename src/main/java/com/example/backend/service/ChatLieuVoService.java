package com.example.backend.service;

import com.example.backend.dto.ChatLieuVoRequest;
import com.example.backend.dto.ChatLieuVoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatLieuVoService {
    ChatLieuVoResponse create(ChatLieuVoRequest request);
    ChatLieuVoResponse update(Long id, ChatLieuVoRequest request);
    void delete(Long id);
    ChatLieuVoResponse getById(Long id);
    Page<ChatLieuVoResponse> search(String keyword, Boolean trangThai, Pageable pageable);
}


