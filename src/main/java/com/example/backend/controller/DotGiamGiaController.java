package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.DotGiamGiaRequest;
import com.example.backend.dto.DotGiamGiaResponse;
import com.example.backend.service.DotGiamGiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dot-giam-gia")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DotGiamGia", description = "API quản lý đợt giảm giá")
@CrossOrigin(origins = "*")
public class DotGiamGiaController {

    private final DotGiamGiaService dotGiamGiaService;

    @PostMapping

    //jkmgvhbjknhvkljhvnklhj

    @Operation(summary = "Tạo đợt giảm giá mới", description = "Tạo một đợt giảm giá mới trong hệ thống")
    public ResponseEntity<ApiResponse<DotGiamGiaResponse>> createDotGiamGia(
            @Valid @RequestBody DotGiamGiaRequest request) {
        try {
            log.info("Creating new DotGiamGia with ma: {}", request.getMaDotGiamGia());
            DotGiamGiaResponse response = dotGiamGiaService.createDotGiamGia(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo đợt giảm giá thành công", response));
        } catch (Exception e) {
            log.error("Error creating DotGiamGia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi tạo đợt giảm giá: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy đợt giảm giá theo ID", description = "Lấy thông tin chi tiết của một đợt giảm giá theo ID")
    public ResponseEntity<ApiResponse<DotGiamGiaResponse>> getDotGiamGiaById(
            @Parameter(description = "ID của đợt giảm giá") @PathVariable Long id) {
        try {
            log.info("Getting DotGiamGia by id: {}", id);
            DotGiamGiaResponse response = dotGiamGiaService.getDotGiamGiaById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đợt giảm giá thành công ", response));
        } catch (Exception e) {
            log.error("Error getting DotGiamGia by id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đợt giảm giá: " + e.getMessage()));
        }
    }

    @GetMapping("/ma/{maDotGiamGia}")
    @Operation(summary = "Lấy đợt giảm giá theo mã", description = "Lấy thông tin chi tiết của một đợt giảm giá theo mã")
    public ResponseEntity<ApiResponse<DotGiamGiaResponse>> getDotGiamGiaByMa(
            @Parameter(description = "Mã của đợt giảm giá") @PathVariable String maDotGiamGia) {
        try {
            log.info("Getting DotGiamGia by ma: {}", maDotGiamGia);
            DotGiamGiaResponse response = dotGiamGiaService.getDotGiamGiaByMa(maDotGiamGia);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đợt giảm giá thành công", response));
        } catch (Exception e) {
            log.error("Error getting DotGiamGia by ma {}: {}", maDotGiamGia, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đợt giảm giá: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật đợt giảm giá", description = "Cập nhật thông tin của một đợt giảm giá")
    public ResponseEntity<ApiResponse<DotGiamGiaResponse>> updateDotGiamGia(
            @Parameter(description = "ID của đợt giảm giá") @PathVariable Long id,
            @Valid @RequestBody DotGiamGiaRequest request) {
        try {
            log.info("Updating DotGiamGia with id: {}", id);
            DotGiamGiaResponse response = dotGiamGiaService.updateDotGiamGia(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật đợt giảm giá thành công", response));
        } catch (Exception e) {
            log.error("Error updating DotGiamGia with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi cập nhật đợt giảm giá: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đợt giảm giá", description = "Xóa một đợt giảm giá khỏi hệ thống")
    public ResponseEntity<ApiResponse<Void>> deleteDotGiamGia(
            @Parameter(description = "ID của đợt giảm giá") @PathVariable Long id) {
        try {
            log.info("Deleting DotGiamGia with id: {}", id);
            dotGiamGiaService.deleteDotGiamGia(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa đợt giảm giá thành công", null));
        } catch (Exception e) {
            log.error("Error deleting DotGiamGia with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi xóa đợt giảm giá: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách đợt giảm giá", description = "Lấy danh sách tất cả đợt giảm giá với phân trang")
    public ResponseEntity<ApiResponse<Page<DotGiamGiaResponse>>> getAllDotGiamGia(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử trên mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường để sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("Getting all DotGiamGia with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                    page, size, sortBy, sortDir);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DotGiamGiaResponse> response = dotGiamGiaService.getAllDotGiamGia(pageable);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đợt giảm giá thành công", response));
        } catch (Exception e) {
            log.error("Error getting all DotGiamGia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách đợt giảm giá: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả đợt giảm giá", description = "Lấy danh sách tất cả đợt giảm giá không phân trang")
    public ResponseEntity<ApiResponse<List<DotGiamGiaResponse>>> getAllDotGiamGiaWithoutPagination() {
        try {
            log.info("Getting all DotGiamGia without pagination");
            List<DotGiamGiaResponse> response = dotGiamGiaService.getAllDotGiamGia();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đợt giảm giá thành công", response));
        } catch (Exception e) {
            log.error("Error getting all DotGiamGia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách đợt giảm giá: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm đợt giảm giá", description = "Tìm kiếm đợt giảm giá theo các tiêu chí")
    public ResponseEntity<ApiResponse<Page<DotGiamGiaResponse>>> searchDotGiamGia(
            @Parameter(description = "Tên đợt giảm giá") @RequestParam(required = false) String tenDotGiamGia,
            @Parameter(description = "Mã đợt giảm giá") @RequestParam(required = false) String maDotGiamGia,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) Boolean trangThai,
            @Parameter(description = "Loại đợt giảm giá") @RequestParam(required = false) String loaiDotGiamGia,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử trên mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường để sắp xếp") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("Searching DotGiamGia with filters - tenDotGiamGia: {}, maDotGiamGia: {}, trangThai: {}, loaiDotGiamGia: {}", 
                    tenDotGiamGia, maDotGiamGia, trangThai, loaiDotGiamGia);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DotGiamGiaResponse> response = dotGiamGiaService.searchDotGiamGia(
                    tenDotGiamGia, maDotGiamGia, trangThai, loaiDotGiamGia, pageable);
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm đợt giảm giá thành công", response));
        } catch (Exception e) {
            log.error("Error searching DotGiamGia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tìm kiếm đợt giảm giá: " + e.getMessage()));
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    @Operation(summary = "Lấy đợt giảm giá theo trạng thái", description = "Lấy danh sách đợt giảm giá theo trạng thái")
    public ResponseEntity<ApiResponse<List<DotGiamGiaResponse>>> getDotGiamGiaByTrangThai(
            @Parameter(description = "Trạng thái của đợt giảm giá") @PathVariable Boolean trangThai) {
        try {
            log.info("Getting DotGiamGia by trangThai: {}", trangThai);
            List<DotGiamGiaResponse> response = dotGiamGiaService.getDotGiamGiaByTrangThai(trangThai);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đợt giảm giá theo trạng thái thành công", response));
        } catch (Exception e) {
            log.error("Error getting DotGiamGia by trangThai {}: {}", trangThai, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách đợt giảm giá theo trạng thái: " + e.getMessage()));
        }
    }

    @GetMapping("/loai/{loaiDotGiamGia}")
    @Operation(summary = "Lấy đợt giảm giá theo loại", description = "Lấy danh sách đợt giảm giá theo loại")
    public ResponseEntity<ApiResponse<List<DotGiamGiaResponse>>> getDotGiamGiaByLoai(
            @Parameter(description = "Loại đợt giảm giá") @PathVariable String loaiDotGiamGia) {
        try {
            log.info("Getting DotGiamGia by loaiDotGiamGia: {}", loaiDotGiamGia);
            List<DotGiamGiaResponse> response = dotGiamGiaService.getDotGiamGiaByLoai(loaiDotGiamGia);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đợt giảm giá theo loại thành công", response));
        } catch (Exception e) {
            log.error("Error getting DotGiamGia by loaiDotGiamGia {}: {}", loaiDotGiamGia, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách đợt giảm giá theo loại: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy đợt giảm giá đang hoạt động", description = "Lấy danh sách các đợt giảm giá đang trong thời gian hiệu lực")
    public ResponseEntity<ApiResponse<List<DotGiamGiaResponse>>> getActiveDotGiamGia() {
        try {
            log.info("Getting active DotGiamGia");
            List<DotGiamGiaResponse> response = dotGiamGiaService.getActiveDotGiamGia();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đợt giảm giá đang hoạt động thành công", response));
        } catch (Exception e) {
            log.error("Error getting active DotGiamGia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách đợt giảm giá đang hoạt động: " + e.getMessage()));
        }
    }

    @GetMapping("/exists/ma/{maDotGiamGia}")
    @Operation(summary = "Kiểm tra mã đợt giảm giá tồn tại", description = "Kiểm tra xem mã đợt giảm giá đã tồn tại trong hệ thống chưa")
    public ResponseEntity<ApiResponse<Boolean>> existsByMaDotGiamGia(
            @Parameter(description = "Mã đợt giảm giá") @PathVariable String maDotGiamGia) {
        try {
            log.info("Checking if DotGiamGia exists by ma: {}", maDotGiamGia);
            boolean exists = dotGiamGiaService.existsByMaDotGiamGia(maDotGiamGia);
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra mã đợt giảm giá thành công", exists));
        } catch (Exception e) {
            log.error("Error checking DotGiamGia exists by ma {}: {}", maDotGiamGia, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi kiểm tra mã đợt giảm giá: " + e.getMessage()));
        }
    }
}
