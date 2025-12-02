package com.example.backend.controller;

import com.example.backend.dto.SanPhamRequest;
import com.example.backend.dto.SanPhamResponse;
import com.example.backend.service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class SanPhamController {


    private final SanPhamService service;

    public SanPhamController(SanPhamService service) {
        this.service = service;
    }

    // ===== PUBLIC ENDPOINTS (Customer - không cần authentication) =====
    @GetMapping("/api/customer/products")
    public ResponseEntity<Page<SanPhamResponse>> getProductsForCustomer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        // Chỉ hiển thị sản phẩm active cho khách hàng
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        // Force chỉ lấy sản phẩm active
        return ResponseEntity.ok(service.search(keyword, true, pageable));
    }

    @GetMapping("/api/customer/products/{id}")
    public ResponseEntity<SanPhamResponse> getProductByIdForCustomer(@PathVariable Long id) {
        SanPhamResponse product = service.getById(id);
        // Chỉ trả về nếu sản phẩm active
        if (product.getTrangThai() != null && product.getTrangThai()) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    // ===== ADMIN ENDPOINTS =====
    @PostMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SanPhamResponse> create(@Valid @RequestBody SanPhamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SanPhamResponse> update(@PathVariable Long id, @Valid @RequestBody SanPhamRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/products/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SanPhamResponse[]> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/api/admin/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<SanPhamResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/api/admin/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<SanPhamResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(service.search(keyword, trangThai, pageable));
    }

    // ===== BACKWARD COMPATIBILITY - Giữ lại các endpoint cũ =====
    @GetMapping("/san-pham")
    public ResponseEntity<Page<SanPhamResponse>> searchLegacy(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        // Redirect to customer endpoint for backward compatibility
        return getProductsForCustomer(keyword, trangThai, page, size, sort);
    }
}


