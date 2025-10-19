package com.example.backend.service;

import com.example.backend.dto.ImeiRequest;
import com.example.backend.dto.ImeiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ImeiService {
    
    // Tạo mới IMEI
    ImeiResponse create(ImeiRequest request);
    
    // Tạo nhiều IMEI cùng lúc
    List<ImeiResponse> createMultiple(List<ImeiRequest> requests);
    
    // Lấy IMEI theo ID
    ImeiResponse getById(Long id);
    
    // Lấy tất cả IMEI theo sản phẩm
    List<ImeiResponse> getBySanPhamId(Long sanPhamId);
    
    // Lấy IMEI theo sản phẩm với phân trang
    Page<ImeiResponse> getBySanPhamId(Long sanPhamId, Pageable pageable);
    
    // Lấy IMEI còn hàng theo sản phẩm
    List<ImeiResponse> getAvailableBySanPhamId(Long sanPhamId);
    
    // Cập nhật trạng thái IMEI
    ImeiResponse updateStatus(Long id, Boolean trangThai);
    
    // Xóa IMEI
    void delete(Long id);
    
    // Xóa tất cả IMEI của sản phẩm
    void deleteBySanPhamId(Long sanPhamId);
    
    // Kiểm tra IMEI có tồn tại không
    boolean existsBySoImei(String soImei);
    
    // Validate IMEI format
    boolean isValidImei(String soImei);
    
    // Import IMEI từ danh sách
    List<ImeiResponse> importImeiList(Long sanPhamId, List<String> imeiList);
    
    // Tìm kiếm IMEI theo số IMEI (partial match)
    Page<ImeiResponse> searchBySoImei(String soImei, Pageable pageable);
    
    // Thống kê IMEI theo sản phẩm
    Map<String, Object> getImeiStatsBySanPham(Long sanPhamId);
    
    // Thống kê IMEI toàn hệ thống
    Map<String, Object> getImeiOverviewStats();
    
    // Export IMEI theo sản phẩm
    byte[] exportImeiBySanPham(Long sanPhamId);
    
    // Cập nhật trạng thái hàng loạt
    List<ImeiResponse> updateBulkStatus(List<Long> imeiIds, Boolean trangThai);
}
