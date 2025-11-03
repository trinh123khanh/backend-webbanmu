package com.example.backend.service;

import com.example.backend.dto.ChiTietSanPhamRequest;
import com.example.backend.dto.ChiTietSanPhamResponse;
import java.util.List;

public interface ChiTietSanPhamService {
    ChiTietSanPhamResponse create(ChiTietSanPhamRequest request);
    ChiTietSanPhamResponse update(Long id, ChiTietSanPhamRequest request);
    void delete(Long id);
    ChiTietSanPhamResponse getById(Long id);
    List<ChiTietSanPhamResponse> getAll();
    List<ChiTietSanPhamResponse> getBySanPhamId(Long sanPhamId);
}
