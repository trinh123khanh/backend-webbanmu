package com.example.backend.service;

import com.example.backend.dto.XuatXuRequest;
import com.example.backend.dto.XuatXuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface XuatXuService {
    XuatXuResponse create(XuatXuRequest request);
    XuatXuResponse update(Long id, XuatXuRequest request);
    void delete(Long id);
    XuatXuResponse getById(Long id);
    Page<XuatXuResponse> search(String keyword, Boolean trangThai, Pageable pageable);
    List<XuatXuResponse> getAllActive();
}


