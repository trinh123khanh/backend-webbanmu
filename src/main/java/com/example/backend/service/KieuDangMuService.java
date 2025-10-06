package com.example.backend.service;

import com.example.backend.dto.KieuDangMuRequest;
import com.example.backend.dto.KieuDangMuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KieuDangMuService {
    KieuDangMuResponse create(KieuDangMuRequest request);
    KieuDangMuResponse update(Long id, KieuDangMuRequest request);
    void delete(Long id);
    KieuDangMuResponse getById(Long id);
    Page<KieuDangMuResponse> search(String keyword, Boolean trangThai, Pageable pageable);
}


