package com.example.backend.service;

import com.example.backend.dto.CongNgheAnToanRequest;
import com.example.backend.dto.CongNgheAnToanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CongNgheAnToanService {
    CongNgheAnToanResponse create(CongNgheAnToanRequest request);
    CongNgheAnToanResponse update(Long id, CongNgheAnToanRequest request);
    void delete(Long id);
    CongNgheAnToanResponse getById(Long id);
    Page<CongNgheAnToanResponse> search(String keyword, Boolean trangThai, Pageable pageable);
    void fixSequence();
}
