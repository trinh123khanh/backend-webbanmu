package com.example.backend.service;

import com.example.backend.dto.DotGiamGiaRequest;
import com.example.backend.dto.DotGiamGiaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DotGiamGiaService {
    
    // Tạo đợt giảm giá mới
    DotGiamGiaResponse createDotGiamGia(DotGiamGiaRequest request);
    
    // Lấy đợt giảm giá theo ID
    DotGiamGiaResponse getDotGiamGiaById(Long id);
    
    // Lấy đợt giảm giá theo mã
    DotGiamGiaResponse getDotGiamGiaByMa(String maDotGiamGia);
    
    // Cập nhật đợt giảm giá
    DotGiamGiaResponse updateDotGiamGia(Long id, DotGiamGiaRequest request);
    
    // Xóa đợt giảm giá
    void deleteDotGiamGia(Long id);
    
    // Lấy tất cả đợt giảm giá (có phân trang)
    Page<DotGiamGiaResponse> getAllDotGiamGia(Pageable pageable);
    
    // Lấy tất cả đợt giảm giá (không phân trang)
    List<DotGiamGiaResponse> getAllDotGiamGia();
    
    // Tìm kiếm đợt giảm giá
    Page<DotGiamGiaResponse> searchDotGiamGia(String tenDotGiamGia, String maDotGiamGia, 
                                             Boolean trangThai, String loaiDotGiamGia, 
                                             Pageable pageable);
    
    // Lấy đợt giảm giá theo trạng thái
    List<DotGiamGiaResponse> getDotGiamGiaByTrangThai(Boolean trangThai);
    
    // Lấy đợt giảm giá theo loại
    List<DotGiamGiaResponse> getDotGiamGiaByLoai(String loaiDotGiamGia);
    
    // Lấy các đợt giảm giá đang hoạt động
    List<DotGiamGiaResponse> getActiveDotGiamGia();
    
    // Kiểm tra mã đợt giảm giá đã tồn tại chưa
    boolean existsByMaDotGiamGia(String maDotGiamGia);
    
    // Kiểm tra mã đợt giảm giá đã tồn tại chưa (trừ id hiện tại)
    boolean existsByMaDotGiamGiaAndIdNot(String maDotGiamGia, Long id);
}
