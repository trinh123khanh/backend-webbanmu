package com.example.backend.service;

import com.example.backend.dto.LoaiMuBaoHiemRequest;
import com.example.backend.dto.LoaiMuBaoHiemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LoaiMuBaoHiemService {
    LoaiMuBaoHiemResponse create(LoaiMuBaoHiemRequest request);
    LoaiMuBaoHiemResponse update(Long id, LoaiMuBaoHiemRequest request);
    void delete(Long id);
    LoaiMuBaoHiemResponse getById(Long id);
    Page<LoaiMuBaoHiemResponse> search(String keyword, Boolean trangThai, Pageable pageable);
    List<LoaiMuBaoHiemResponse> getAllActive();
}
