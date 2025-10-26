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

import java.util.List;

@RestController
@RequestMapping("/api/phieu-giam-gia-ca-nhan")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
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
}
