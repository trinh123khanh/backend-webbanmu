package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ImeiRequest;
import com.example.backend.dto.ImeiResponse;
import com.example.backend.service.ImeiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imei")
@CrossOrigin(origins = "*")
public class ImeiController {
    
    @Autowired
    private ImeiService imeiService;
    
    // Tạo mới IMEI
    @PostMapping
    public ResponseEntity<ApiResponse<ImeiResponse>> create(@Valid @RequestBody ImeiRequest request) {
        try {
            ImeiResponse response = imeiService.create(request);
            return ResponseEntity.ok(ApiResponse.success("Tạo IMEI thành công", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Tạo nhiều IMEI
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<ImeiResponse>>> createMultiple(@Valid @RequestBody List<ImeiRequest> requests) {
        try {
            List<ImeiResponse> responses = imeiService.createMultiple(requests);
            return ResponseEntity.ok(ApiResponse.success("Tạo IMEI thành công", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Lấy IMEI theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImeiResponse>> getById(@PathVariable Long id) {
        try {
            ImeiResponse response = imeiService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Lấy tất cả IMEI theo sản phẩm
    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<ApiResponse<List<ImeiResponse>>> getBySanPhamId(@PathVariable Long sanPhamId) {
        try {
            List<ImeiResponse> responses = imeiService.getBySanPhamId(sanPhamId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Lấy IMEI theo sản phẩm với phân trang
    @GetMapping("/san-pham/{sanPhamId}/page")
    public ResponseEntity<ApiResponse<Page<ImeiResponse>>> getBySanPhamIdWithPagination(
            @PathVariable Long sanPhamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ImeiResponse> responses = imeiService.getBySanPhamId(sanPhamId, pageable);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Lấy IMEI còn hàng theo sản phẩm
    @GetMapping("/san-pham/{sanPhamId}/available")
    public ResponseEntity<ApiResponse<List<ImeiResponse>>> getAvailableBySanPhamId(@PathVariable Long sanPhamId) {
        try {
            List<ImeiResponse> responses = imeiService.getAvailableBySanPhamId(sanPhamId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Cập nhật trạng thái IMEI
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ImeiResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean trangThai) {
        try {
            ImeiResponse response = imeiService.updateStatus(id, trangThai);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Xóa IMEI
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            imeiService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa IMEI thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Xóa tất cả IMEI của sản phẩm
    @DeleteMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<ApiResponse<Void>> deleteBySanPhamId(@PathVariable Long sanPhamId) {
        try {
            imeiService.deleteBySanPhamId(sanPhamId);
            return ResponseEntity.ok(ApiResponse.success("Xóa tất cả IMEI thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Kiểm tra IMEI có tồn tại không
    @GetMapping("/exists/{soImei}")
    public ResponseEntity<ApiResponse<Boolean>> existsBySoImei(@PathVariable String soImei) {
        try {
            boolean exists = imeiService.existsBySoImei(soImei);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Validate IMEI format
    @GetMapping("/validate/{soImei}")
    public ResponseEntity<ApiResponse<Boolean>> validateImei(@PathVariable String soImei) {
        try {
            boolean isValid = imeiService.isValidImei(soImei);
            return ResponseEntity.ok(ApiResponse.success(isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Import IMEI từ danh sách
    @PostMapping("/import/{sanPhamId}")
    public ResponseEntity<ApiResponse<List<ImeiResponse>>> importImeiList(
            @PathVariable Long sanPhamId,
            @RequestBody List<String> imeiList) {
        try {
            List<ImeiResponse> responses = imeiService.importImeiList(sanPhamId, imeiList);
            return ResponseEntity.ok(ApiResponse.success("Import IMEI thành công", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Tìm kiếm IMEI theo số IMEI (partial match)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ImeiResponse>>> searchImei(
            @RequestParam String soImei,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ImeiResponse> responses = imeiService.searchBySoImei(soImei, pageable);
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành công", responses.getContent()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Thống kê IMEI theo sản phẩm
    @GetMapping("/stats/san-pham/{sanPhamId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImeiStatsBySanPham(@PathVariable Long sanPhamId) {
        try {
            Map<String, Object> stats = imeiService.getImeiStatsBySanPham(sanPhamId);
            return ResponseEntity.ok(ApiResponse.success("Thống kê thành công", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Thống kê IMEI toàn hệ thống
    @GetMapping("/stats/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImeiOverviewStats() {
        try {
            Map<String, Object> stats = imeiService.getImeiOverviewStats();
            return ResponseEntity.ok(ApiResponse.success("Thống kê tổng quan thành công", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Export IMEI theo sản phẩm
    @GetMapping("/export/san-pham/{sanPhamId}")
    public ResponseEntity<byte[]> exportImeiBySanPham(@PathVariable Long sanPhamId) {
        try {
            byte[] csvData = imeiService.exportImeiBySanPham(sanPhamId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "imei_san_pham_" + sanPhamId + ".csv");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Cập nhật trạng thái hàng loạt
    @PutMapping("/bulk/status")
    public ResponseEntity<ApiResponse<List<ImeiResponse>>> updateBulkStatus(
            @RequestBody List<Long> imeiIds,
            @RequestParam Boolean trangThai) {
        try {
            List<ImeiResponse> responses = imeiService.updateBulkStatus(imeiIds, trangThai);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái hàng loạt thành công", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
