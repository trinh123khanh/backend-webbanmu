package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PhieuGiamGiaCaNhanRequest;
import com.example.backend.dto.PhieuGiamGiaCaNhanResponse;
import com.example.backend.service.PhieuGiamGiaCaNhanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RestController
@RequestMapping("/api/phieu-giam-gia-ca-nhan")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class PhieuGiamGiaCaNhanController {
    
    private final PhieuGiamGiaCaNhanService phieuGiamGiaCaNhanService;
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PhieuGiamGiaCaNhanResponse>>> getAllPhieuGiamGiaCaNhan() {
        log.info("API: Lấy tất cả phiếu giảm giá cá nhân");
        
        ApiResponse<List<PhieuGiamGiaCaNhanResponse>> response = phieuGiamGiaCaNhanService.getAllPhieuGiamGiaCaNhan();
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân với phân trang
     */
    @GetMapping("/pagination")
    public ResponseEntity<ApiResponse<Page<PhieuGiamGiaCaNhanResponse>>> getAllPhieuGiamGiaCaNhanWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("API: Lấy tất cả phiếu giảm giá cá nhân với phân trang - Page: {}, Size: {}", page, size);
        
        ApiResponse<Page<PhieuGiamGiaCaNhanResponse>> response = 
                phieuGiamGiaCaNhanService.getAllPhieuGiamGiaCaNhanWithPagination(page, size);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PhieuGiamGiaCaNhanResponse>> getPhieuGiamGiaCaNhanById(@PathVariable Long id) {
        log.info("API: Lấy phiếu giảm giá cá nhân theo ID: {}", id);
        
        ApiResponse<PhieuGiamGiaCaNhanResponse> response = phieuGiamGiaCaNhanService.getPhieuGiamGiaCaNhanById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo khách hàng
     */
    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<ApiResponse<List<PhieuGiamGiaCaNhanResponse>>> getPhieuGiamGiaCaNhanByKhachHang(
            @PathVariable Long khachHangId) {
        log.info("API: Lấy phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId);
        
        ApiResponse<List<PhieuGiamGiaCaNhanResponse>> response = 
                phieuGiamGiaCaNhanService.getPhieuGiamGiaCaNhanByKhachHang(khachHangId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo khách hàng với phân trang
     */
    @GetMapping("/khach-hang/{khachHangId}/pagination")
    public ResponseEntity<ApiResponse<Page<PhieuGiamGiaCaNhanResponse>>> getPhieuGiamGiaCaNhanByKhachHangWithPagination(
            @PathVariable Long khachHangId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("API: Lấy phiếu giảm giá cá nhân theo khách hàng ID: {} với phân trang - Page: {}, Size: {}", 
                khachHangId, page, size);
        
        ApiResponse<Page<PhieuGiamGiaCaNhanResponse>> response = 
                phieuGiamGiaCaNhanService.getPhieuGiamGiaCaNhanByKhachHangWithPagination(khachHangId, page, size);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    
    /**
     * Tạo mới phiếu giảm giá cá nhân
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PhieuGiamGiaCaNhanResponse>> createPhieuGiamGiaCaNhan(
            @RequestBody PhieuGiamGiaCaNhanRequest request) {
        log.info("API: Tạo mới phiếu giảm giá cá nhân cho khách hàng ID: {}, phiếu giảm giá ID: {}", 
                request.getKhachHangId(), request.getPhieuGiamGiaId());
        
        ApiResponse<PhieuGiamGiaCaNhanResponse> response = phieuGiamGiaCaNhanService.createPhieuGiamGiaCaNhan(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật phiếu giảm giá cá nhân
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PhieuGiamGiaCaNhanResponse>> updatePhieuGiamGiaCaNhan(
            @PathVariable Long id,
            @RequestBody PhieuGiamGiaCaNhanRequest request) {
        log.info("API: Cập nhật phiếu giảm giá cá nhân ID: {}", id);
        
        ApiResponse<PhieuGiamGiaCaNhanResponse> response = phieuGiamGiaCaNhanService.updatePhieuGiamGiaCaNhan(id, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa phiếu giảm giá cá nhân
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePhieuGiamGiaCaNhan(@PathVariable Long id) {
        log.info("API: Xóa phiếu giảm giá cá nhân ID: {}", id);
        
        ApiResponse<Void> response = phieuGiamGiaCaNhanService.deletePhieuGiamGiaCaNhan(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    /**
     * Lấy thống kê phiếu giảm giá cá nhân theo khách hàng
     */
    @GetMapping("/statistics/khach-hang/{khachHangId}")
    public ResponseEntity<ApiResponse<Object>> getStatisticsByKhachHang(@PathVariable Long khachHangId) {
        log.info("API: Lấy thống kê phiếu giảm giá cá nhân cho khách hàng ID: {}", khachHangId);
        
        ApiResponse<Object> response = phieuGiamGiaCaNhanService.getStatisticsByKhachHang(khachHangId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật toàn bộ khách hàng cho một phiếu giảm giá
     * Xóa tất cả customer cũ và thêm customer mới
     */
    @PutMapping("/phieu/{phieuGiamGiaId}")
    public ResponseEntity<ApiResponse<List<PhieuGiamGiaCaNhanResponse>>> updateCustomersForPhieu(
            @PathVariable Long phieuGiamGiaId,
            @RequestBody java.util.Map<String, Object> requestBody) {
        log.info("API: Cập nhật khách hàng cho phiếu giảm giá ID: {}", phieuGiamGiaId);
        log.info("Request body: {}", requestBody);
        
        try {
            // Extract and convert khachHangIds from request body
            Object khachHangIdsObj = requestBody.get("khachHangIds");
            List<Long> khachHangIds = new java.util.ArrayList<>();
            
            if (khachHangIdsObj != null) {
                if (khachHangIdsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> idList = (List<Object>) khachHangIdsObj;
                    
                    for (Object id : idList) {
                        if (id instanceof Number) {
                            khachHangIds.add(((Number) id).longValue());
                        } else if (id instanceof String) {
                            khachHangIds.add(Long.parseLong((String) id));
                        }
                    }
                }
            }
            
            log.info("Converted khachHangIds: {}", khachHangIds);
            
            ApiResponse<List<PhieuGiamGiaCaNhanResponse>> response = 
                    phieuGiamGiaCaNhanService.updateCustomersForPhieu(phieuGiamGiaId, khachHangIds);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi parse request body", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Lỗi khi xử lý dữ liệu: " + e.getMessage())
            );
        }
    }
    
    /**
     * Xóa tất cả khách hàng cho một phiếu giảm giá
     */
    @DeleteMapping("/phieu/{phieuGiamGiaId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomersByPhieuId(@PathVariable Long phieuGiamGiaId) {
        log.info("API: Xóa tất cả khách hàng cho phiếu giảm giá ID: {}", phieuGiamGiaId);
        
        try {
            phieuGiamGiaCaNhanService.deletePhieuGiamGiaCaNhanByPhieuGiamGiaId(phieuGiamGiaId);
            return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công", null));
        } catch (Exception e) {
            log.error("Lỗi khi xóa khách hàng cho phiếu ID: {}", phieuGiamGiaId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi xóa khách hàng: " + e.getMessage()));
        }
    }
}
