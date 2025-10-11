package com.example.backend.service;

import com.example.backend.dto.SanPhamRequest;
import com.example.backend.dto.SanPhamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SanPhamService {
    SanPhamResponse create(SanPhamRequest request);
    SanPhamResponse update(Long id, SanPhamRequest request);
    void delete(Long id);
    SanPhamResponse getById(Long id);
    Page<SanPhamResponse> search(String keyword, Boolean trangThai, Pageable pageable);
    SanPhamResponse[] getAll();
}


