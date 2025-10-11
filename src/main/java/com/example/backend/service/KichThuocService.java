package com.example.backend.service;

import com.example.backend.dto.KichThuocRequest;
import com.example.backend.dto.KichThuocResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KichThuocService {
    KichThuocResponse create(KichThuocRequest request);
    KichThuocResponse update(Long id, KichThuocRequest request);
    void delete(Long id);
    KichThuocResponse getById(Long id);
    Page<KichThuocResponse> search(String keyword, Boolean trangThai, Pageable pageable);
    void fixSequence();
}


