package com.example.backend.service;

import com.example.backend.dto.MauSacRequest;
import com.example.backend.dto.MauSacResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MauSacService {
    MauSacResponse create(MauSacRequest request);
    MauSacResponse update(Long id, MauSacRequest request);
    void delete(Long id);
    MauSacResponse getById(Long id);
    Page<MauSacResponse> search(String keyword, Boolean trangThai, Pageable pageable);
}


