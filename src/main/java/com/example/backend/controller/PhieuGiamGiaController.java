package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PhieuGiamGiaController {
    
    private final PhieuGiamGiaService phieuGiamGiaService;
    
    // Tạo phiếu giảm giá mới
    @PostMapping
    public ResponseEntity<ApiResponse<PhieuGiamGiaResponse>> createPhieuGiamGia(
            @Valid @RequestBody PhieuGiamGiaRequest request) {
        log.info("Tạo phiếu giảm giá mới: {}", request.getMaPhieu());
        
        ApiResponse<PhieuGiamGiaResponse> response = phieuGiamGiaService.createPhieuGiamGia(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Cập nhật phiếu giảm giá
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PhieuGiamGiaResponse>> updatePhieuGiamGia(
            @PathVariable Long id,
            @Valid @RequestBody PhieuGiamGiaRequest request) {
        log.info("Cập nhật phiếu giảm giá ID: {}", id);
        
        ApiResponse<PhieuGiamGiaResponse> response = phieuGiamGiaService.updatePhieuGiamGia(id, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Toggle trạng thái phiếu giảm giá
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<PhieuGiamGiaResponse>> togglePhieuGiamGiaStatus(@PathVariable Long id) {
        log.info("Toggle trạng thái phiếu giảm giá ID: {}", id);
        
        ApiResponse<PhieuGiamGiaResponse> response = phieuGiamGiaService.togglePhieuGiamGiaStatus(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Xóa phiếu giảm giá
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePhieuGiamGia(@PathVariable Long id) {
        log.info("Xóa phiếu giảm giá ID: {}", id);
        
        ApiResponse<String> response = phieuGiamGiaService.deletePhieuGiamGia(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy danh sách khách hàng cho form phiếu giảm giá
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<java.util.List<com.example.backend.dto.KhachHangDTO>>> getAllCustomersForVoucher() {
        log.info("Lấy danh sách khách hàng cho form phiếu giảm giá");
        
        ApiResponse<java.util.List<com.example.backend.dto.KhachHangDTO>> response = phieuGiamGiaService.getAllCustomersForVoucher();
        
        return ResponseEntity.ok(response);
    }
    
    // Lấy tất cả phiếu giảm giá với trạng thái động được tính toán
    @GetMapping("/dynamic-status")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> getAllPhieuGiamGiaWithDynamicStatus() {
        log.info("Lấy tất cả phiếu giảm giá với trạng thái động");
        
        ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = phieuGiamGiaService.getAllPhieuGiamGiaWithDynamicStatus();
        
        return ResponseEntity.ok(response);
    }
    
    // Lấy phiếu giảm giá theo trạng thái động (sắp diễn ra, đang diễn ra, kết thúc)
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> getPhieuGiamGiaByDynamicStatus(@PathVariable String status) {
        log.info("Lấy phiếu giảm giá theo trạng thái động: {}", status);
        
        ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = phieuGiamGiaService.getPhieuGiamGiaByDynamicStatus(status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy phiếu giảm giá theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PhieuGiamGiaResponse>> getPhieuGiamGiaById(@PathVariable Long id) {
        log.info("Lấy phiếu giảm giá theo ID: {}", id);
        
        ApiResponse<PhieuGiamGiaResponse> response = phieuGiamGiaService.getPhieuGiamGiaById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Lấy phiếu giảm giá theo mã phiếu
    @GetMapping("/ma-phieu/{maPhieu}")
    public ResponseEntity<ApiResponse<PhieuGiamGiaResponse>> getPhieuGiamGiaByMaPhieu(
            @PathVariable String maPhieu) {
        log.info("Lấy phiếu giảm giá theo mã: {}", maPhieu);
        
        ApiResponse<PhieuGiamGiaResponse> response = phieuGiamGiaService.getPhieuGiamGiaByMaPhieu(maPhieu);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Lấy danh sách phiếu giảm giá với phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<PhieuGiamGiaListResponse>> getAllPhieuGiamGia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Lấy danh sách phiếu giảm giá - Page: {}, Size: {}, Sort: {} {}", 
                page, size, sortBy, sortDir);
        
        ApiResponse<PhieuGiamGiaListResponse> response = phieuGiamGiaService.getAllPhieuGiamGia(page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(response);
    }
    
    // Lấy phiếu giảm giá đang hoạt động
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> getActivePhieuGiamGia() {
        log.info("Lấy phiếu giảm giá đang hoạt động");
        
        ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = phieuGiamGiaService.getActivePhieuGiamGia();
        
        return ResponseEntity.ok(response);
    }
    
    // Tìm kiếm phiếu giảm giá
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> searchPhieuGiamGia(
            @RequestParam String keyword) {
        log.info("Tìm kiếm phiếu giảm giá với từ khóa: {}", keyword);
        
        ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = phieuGiamGiaService.searchPhieuGiamGia(keyword);
        
        return ResponseEntity.ok(response);
    }
    
    // Đếm số lượng phiếu giảm giá đang hoạt động
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> getActiveVoucherCount() {
        log.info("Đếm số lượng phiếu giảm giá đang hoạt động");
        
        ApiResponse<Long> response = phieuGiamGiaService.getActiveVoucherCount();
        
        return ResponseEntity.ok(response);
    }
    
    // Lấy phiếu giảm giá sắp hết hạn
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> getExpiringSoonVouchers(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Lấy phiếu giảm giá sắp hết hạn trong {} ngày", days);
        
        ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = phieuGiamGiaService.getExpiringSoonVouchers(days);
        
        return ResponseEntity.ok(response);
    }
    
    // Lấy phiếu giảm giá theo loại
    @GetMapping("/loai/{loaiPhieuGiamGia}")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> getPhieuGiamGiaByLoai(
            @PathVariable Boolean loaiPhieuGiamGia) {
        log.info("Lấy phiếu giảm giá theo loại: {}", loaiPhieuGiamGia ? "Tiền mặt" : "Phần trăm");
        
        // Tạo request để lấy danh sách
        ApiResponse<PhieuGiamGiaListResponse> listResponse = phieuGiamGiaService.getAllPhieuGiamGia(0, 1000, "id", "asc");
        
        if (listResponse.isSuccess() && listResponse.getData() != null) {
            java.util.List<PhieuGiamGiaResponse> filteredList = listResponse.getData().getData().stream()
                    .filter(p -> p.getLoaiPhieuGiamGia().equals(loaiPhieuGiamGia))
                    .collect(java.util.stream.Collectors.toList());
            
            ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = ApiResponse.success(filteredList);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá"));
        }
    }
    
    // Lấy phiếu giảm giá theo trạng thái
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<ApiResponse<java.util.List<PhieuGiamGiaResponse>>> getPhieuGiamGiaByTrangThai(
            @PathVariable Boolean trangThai) {
        log.info("Lấy phiếu giảm giá theo trạng thái: {}", trangThai ? "Hoạt động" : "Không hoạt động");
        
        // Tạo request để lấy danh sách
        ApiResponse<PhieuGiamGiaListResponse> listResponse = phieuGiamGiaService.getAllPhieuGiamGia(0, 1000, "id", "asc");
        
        if (listResponse.isSuccess() && listResponse.getData() != null) {
            java.util.List<PhieuGiamGiaResponse> filteredList = listResponse.getData().getData().stream()
                    .filter(p -> p.getTrangThai().equals(trangThai))
                    .collect(java.util.stream.Collectors.toList());
            
            ApiResponse<java.util.List<PhieuGiamGiaResponse>> response = ApiResponse.success(filteredList);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá"));
        }
    }
}
