package com.example.backend.service;

import com.example.backend.dto.TrongLuongRequest;
import com.example.backend.dto.TrongLuongResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TrongLuongService {
    TrongLuongResponse create(TrongLuongRequest request);
    TrongLuongResponse update(Long id, TrongLuongRequest request);
    void delete(Long id);
    TrongLuongResponse getById(Long id);
    Page<TrongLuongResponse> search(String keyword, Boolean trangThai, Pageable pageable);
    List<TrongLuongResponse> getAllActive();
}
