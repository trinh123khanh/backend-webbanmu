package com.example.backend.service;

import com.example.backend.dto.NhaSanXuatRequest;
import com.example.backend.dto.NhaSanXuatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NhaSanXuatService {
    NhaSanXuatResponse create(NhaSanXuatRequest request);
    NhaSanXuatResponse update(Long id, NhaSanXuatRequest request);
    void delete(Long id);
    NhaSanXuatResponse getById(Long id);
    Page<NhaSanXuatResponse> search(String keyword, Boolean trangThai, Pageable pageable);
}



