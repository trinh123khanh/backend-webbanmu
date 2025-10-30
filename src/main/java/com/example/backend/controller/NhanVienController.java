package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.NhanVienDTO;
import com.example.backend.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienService nhanVienService;

    // Lấy tất cả nhân viên với phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NhanVienDTO>>> getAllNhanVien(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Page<NhanVienDTO> nhanVienPage = nhanVienService.getAllNhanVien(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", nhanVienPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách nhân viên: " + e.getMessage()));
        }
    }

    // Lấy nhân viên theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NhanVienDTO>> getNhanVienById(@PathVariable Long id) {
        try {
            Optional<NhanVienDTO> nhanVien = nhanVienService.getNhanVienById(id);
            if (nhanVien.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", nhanVien.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin nhân viên: " + e.getMessage()));
        }
    }

    // Tìm kiếm nhân viên với bộ lọc
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<NhanVienDTO>>> searchNhanVien(
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String soDienThoai,
            @RequestParam(required = false) String maNhanVien,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Page<NhanVienDTO> nhanVienPage = nhanVienService.searchNhanVien(
                hoTen, email, soDienThoai, maNhanVien, trangThai, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm nhân viên thành công", nhanVienPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tìm kiếm nhân viên: " + e.getMessage()));
        }
    }

    // Tạo nhân viên mới
    @PostMapping
    public ResponseEntity<ApiResponse<NhanVienDTO>> createNhanVien(@RequestBody NhanVienDTO nhanVienDTO) {
        try {
            // Tự động tạo mã nhân viên nếu không có
            if (nhanVienDTO.getMaNhanVien() == null || nhanVienDTO.getMaNhanVien().trim().isEmpty()) {
                nhanVienDTO.setMaNhanVien(nhanVienService.generateMaNhanVien());
            }
            
            NhanVienDTO createdNhanVien = nhanVienService.createNhanVien(nhanVienDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo nhân viên thành công", createdNhanVien));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo nhân viên: " + e.getMessage()));
        }
    }

    // Cập nhật nhân viên
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NhanVienDTO>> updateNhanVien(@PathVariable Long id, @RequestBody NhanVienDTO nhanVienDTO) {
        try {
            Optional<NhanVienDTO> updatedNhanVien = nhanVienService.updateNhanVien(id, nhanVienDTO);
            if (updatedNhanVien.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công", updatedNhanVien.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với ID: " + id));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật nhân viên: " + e.getMessage()));
        }
    }

    // Xóa nhân viên (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNhanVien(@PathVariable Long id) {
        try {
            boolean deleted = nhanVienService.deleteNhanVien(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa nhân viên: " + e.getMessage()));
        }
    }

    // Xóa vĩnh viễn nhân viên
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteNhanVien(@PathVariable Long id) {
        try {
            boolean deleted = nhanVienService.permanentlyDeleteNhanVien(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Xóa vĩnh viễn nhân viên thành công", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa vĩnh viễn nhân viên: " + e.getMessage()));
        }
    }

    // Lấy nhân viên theo email
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<NhanVienDTO>> getNhanVienByEmail(@PathVariable String email) {
        try {
            Optional<NhanVienDTO> nhanVien = nhanVienService.getNhanVienByEmail(email);
            if (nhanVien.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", nhanVien.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với email: " + email));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin nhân viên: " + e.getMessage()));
        }
    }

    // Lấy nhân viên theo số điện thoại
    @GetMapping("/phone/{soDienThoai}")
    public ResponseEntity<ApiResponse<NhanVienDTO>> getNhanVienBySoDienThoai(@PathVariable String soDienThoai) {
        try {
            Optional<NhanVienDTO> nhanVien = nhanVienService.getNhanVienBySoDienThoai(soDienThoai);
            if (nhanVien.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", nhanVien.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với số điện thoại: " + soDienThoai));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin nhân viên: " + e.getMessage()));
        }
    }

    // Lấy nhân viên theo mã nhân viên
    @GetMapping("/ma-nhan-vien/{maNhanVien}")
    public ResponseEntity<ApiResponse<NhanVienDTO>> getNhanVienByMaNhanVien(@PathVariable String maNhanVien) {
        try {
            Optional<NhanVienDTO> nhanVien = nhanVienService.getNhanVienByMaNhanVien(maNhanVien);
            if (nhanVien.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", nhanVien.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy nhân viên với mã: " + maNhanVien));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin nhân viên: " + e.getMessage()));
        }
    }

    // Lấy nhân viên theo trạng thái
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<ApiResponse<List<NhanVienDTO>>> getNhanVienByTrangThai(@PathVariable Boolean trangThai) {
        try {
            List<NhanVienDTO> nhanVienList = nhanVienService.getNhanVienByTrangThai(trangThai);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", nhanVienList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách nhân viên: " + e.getMessage()));
        }
    }

    // Thống kê nhân viên
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getNhanVienStats() {
        try {
            long totalActive = nhanVienService.countNhanVienByTrangThai(true);
            long totalInactive = nhanVienService.countNhanVienByTrangThai(false);
            
            Object stats = new Object() {
                public final long activeCount = totalActive;
                public final long inactiveCount = totalInactive;
                public final long totalCount = totalActive + totalInactive;
            };
            
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê nhân viên thành công", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thống kê nhân viên: " + e.getMessage()));
        }
    }

    // Lấy nhân viên theo khoảng thời gian vào làm
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<NhanVienDTO>>> getNhanVienByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<NhanVienDTO> nhanVienList = nhanVienService.getNhanVienByDateRange(start, end);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", nhanVienList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách nhân viên: " + e.getMessage()));
        }
    }

    // Tạo mã nhân viên mới
    @GetMapping("/generate-ma-nhan-vien")
    public ResponseEntity<ApiResponse<String>> generateMaNhanVien() {
        try {
            String maNhanVien = nhanVienService.generateMaNhanVien();
            return ResponseEntity.ok(ApiResponse.success("Tạo mã nhân viên thành công", maNhanVien));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo mã nhân viên: " + e.getMessage()));
        }
    }

    // Kiểm tra email đã tồn tại
    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Object>> checkEmailExists(@PathVariable String email) {
        try {
            boolean emailExists = nhanVienService.existsByEmail(email);
            Object result = new Object() {
                public final boolean exists = emailExists;
            };
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra email thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi kiểm tra email: " + e.getMessage()));
        }
    }

    // Kiểm tra số điện thoại đã tồn tại
    @GetMapping("/check-phone/{soDienThoai}")
    public ResponseEntity<ApiResponse<Object>> checkPhoneExists(@PathVariable String soDienThoai) {
        try {
            boolean phoneExists = nhanVienService.existsBySoDienThoai(soDienThoai);
            Object result = new Object() {
                public final boolean exists = phoneExists;
            };
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra số điện thoại thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi kiểm tra số điện thoại: " + e.getMessage()));
        }
    }

    // Kiểm tra mã nhân viên đã tồn tại
    @GetMapping("/check-ma-nhan-vien/{maNhanVien}")
    public ResponseEntity<ApiResponse<Object>> checkMaNhanVienExists(@PathVariable String maNhanVien) {
        try {
            boolean maExists = nhanVienService.existsByMaNhanVien(maNhanVien);
            Object result = new Object() {
                public final boolean exists = maExists;
            };
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra mã nhân viên thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi kiểm tra mã nhân viên: " + e.getMessage()));
        }
    }
}
