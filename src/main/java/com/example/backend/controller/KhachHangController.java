package com.example.backend.controller;

import com.example.backend.dto.KhachHangDTO;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.KhachHangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@Slf4j
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;
    
    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả khách hàng với phân trang
    @GetMapping
    public ResponseEntity<Page<KhachHangDTO>> getAllKhachHang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Page<KhachHangDTO> khachHangPage = khachHangService.getAllKhachHang(page, size, sortBy, sortDir);
            return ResponseEntity.ok(khachHangPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Tìm kiếm khách hàng với bộ lọc
    @GetMapping("/search")
    public ResponseEntity<Page<KhachHangDTO>> searchKhachHang(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Page<KhachHangDTO> khachHangPage = khachHangService.searchKhachHang(keyword, trangThai, page, size, sortBy, sortDir);
            return ResponseEntity.ok(khachHangPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // Lấy thông tin khách hàng hiện tại từ JWT token (username)
    // QUAN TRỌNG: Route này PHẢI được đặt TRƯỚC route /{id} để tránh conflict
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentCustomer() {
        try {
            // Lấy username từ JWT token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
                log.warn("⚠️ Unauthorized access to /me endpoint");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("message", "Bạn cần đăng nhập để xem thông tin cá nhân");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            String username = auth.getName();
            log.info("📋 API: Lấy thông tin khách hàng hiện tại từ username: {}", username);
            
            // Tìm User từ username
            var userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                log.error("❌ Không tìm thấy user với username: {}", username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("message", "Không tìm thấy tài khoản với username: " + username);
                errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            var user = userOptional.get();
            log.info("✅ Tìm thấy user: {} (ID: {})", username, user.getId());
            
            // Tìm KhachHang từ user_id
            Optional<KhachHangDTO> khachHangOptional = khachHangService.getKhachHangByUserId(user.getId());
            
            if (khachHangOptional.isPresent()) {
                KhachHangDTO khachHang = khachHangOptional.get();
                log.info("✅ Tìm thấy khách hàng cho user: {}, Khách hàng ID: {}", username, khachHang.getId());
                return ResponseEntity.ok(khachHang);
            } else {
                // Nếu chưa có KhachHang, tự động tạo một record mới
                log.warn("⚠️ Không tìm thấy khách hàng cho user: {}, đang tạo mới...", username);
                try {
                    KhachHangDTO newKhachHangDTO = khachHangService.createKhachHangFromUser(user);
                    log.info("✅ Đã tạo khách hàng mới cho user: {}, Khách hàng ID: {}", username, newKhachHangDTO.getId());
                    return ResponseEntity.ok(newKhachHangDTO);
                } catch (Exception createEx) {
                    log.error("❌ Không thể tạo khách hàng mới cho user: {}", username, createEx);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", true);
                    errorResponse.put("message", "Không thể tạo thông tin khách hàng: " + createEx.getMessage());
                    errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            }
        } catch (RuntimeException e) {
            log.error("❌ RuntimeException khi lấy thông tin khách hàng hiện tại: {}", e.getMessage(), e);
            // Trả về error response dưới dạng JSON
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lấy thông tin khách hàng hiện tại", e);
            // Trả về error response dưới dạng JSON
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Lỗi khi lấy thông tin khách hàng: " + e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Lấy khách hàng theo mã
    @GetMapping("/ma/{maKhachHang}")
    public ResponseEntity<KhachHangDTO> getKhachHangByMa(@PathVariable String maKhachHang) {
        try {
            Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangByMa(maKhachHang);
            return khachHang.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy khách hàng theo email
    @GetMapping("/email/{email}")
    public ResponseEntity<KhachHangDTO> getKhachHangByEmail(@PathVariable String email) {
        try {
            Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangByEmail(email);
            return khachHang.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy khách hàng theo số điện thoại
    @GetMapping("/sdt/{soDienThoai}")
    public ResponseEntity<KhachHangDTO> getKhachHangBySoDienThoai(@PathVariable String soDienThoai) {
        try {
            Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangBySoDienThoai(soDienThoai);
            return khachHang.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy khách hàng theo User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<KhachHangDTO> getKhachHangByUserId(@PathVariable Long userId) {
        try {
            Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangByUserId(userId);
            return khachHang.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy chi tiết khách hàng theo ID (bao gồm địa chỉ mặc định)
    // QUAN TRỌNG: Route này PHẢI được đặt SAU các route cụ thể như /me, /ma/{maKhachHang}, etc.
    @GetMapping("/{id}")
    public ResponseEntity<?> getKhachHangById(@PathVariable Long id) {
        try {
            log.info("📋 API: Lấy chi tiết khách hàng theo ID: {}", id);
            Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangById(id);
            if (khachHang.isPresent()) {
                log.info("✅ Tìm thấy khách hàng ID: {}, Tên: {}", id, khachHang.get().getTenKhachHang());
                return ResponseEntity.ok(khachHang.get());
            } else {
                log.warn("⚠️ Không tìm thấy khách hàng với ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy khách hàng với ID: " + id);
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi lấy thông tin khách hàng ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy thông tin khách hàng: " + e.getMessage());
        }
    }

    // Tạo khách hàng mới
    @PostMapping
    public ResponseEntity<?> createKhachHang(@RequestBody KhachHangDTO khachHangDTO) {
        try {
            KhachHangDTO createdKhachHang = khachHangService.createKhachHang(khachHangDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdKhachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }

    // Cập nhật khách hàng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKhachHang(@PathVariable Long id, @RequestBody KhachHangDTO khachHangDTO) {
        try {
            KhachHangDTO updatedKhachHang = khachHangService.updateKhachHang(id, khachHangDTO);
            return ResponseEntity.ok(updatedKhachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }

    // Xóa vĩnh viễn khách hàng (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKhachHang(@PathVariable Long id) {
        try {
            khachHangService.deleteKhachHangPermanently(id);
            return ResponseEntity.ok("Đã xóa vĩnh viễn khách hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }

    // Xóa mềm khách hàng (cập nhật trạng thái thành không hoạt động)
    @DeleteMapping("/{id}/soft")
    public ResponseEntity<?> softDeleteKhachHang(@PathVariable Long id) {
        try {
            khachHangService.softDeleteKhachHang(id);
            return ResponseEntity.ok("Đã chuyển khách hàng sang trạng thái không hoạt động");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }

    // Xóa cứng khách hàng (endpoint cũ - giữ để tương thích)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> deleteKhachHangPermanently(@PathVariable Long id) {
        try {
            khachHangService.deleteKhachHangPermanently(id);
            return ResponseEntity.ok("Đã xóa vĩnh viễn khách hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }

    // Kiểm tra email đã tồn tại
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = khachHangService.checkEmailExists(email);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Kiểm tra số điện thoại đã tồn tại
    @GetMapping("/check-sdt/{soDienThoai}")
    public ResponseEntity<Boolean> checkSoDienThoaiExists(@PathVariable String soDienThoai) {
        try {
            boolean exists = khachHangService.checkSoDienThoaiExists(soDienThoai);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Kiểm tra mã khách hàng đã tồn tại
    @GetMapping("/check-ma/{maKhachHang}")
    public ResponseEntity<Boolean> checkMaKhachHangExists(@PathVariable String maKhachHang) {
        try {
            boolean exists = khachHangService.checkMaKhachHangExists(maKhachHang);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy thống kê
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            long total = khachHangService.getTotalKhachHang();
            long active = khachHangService.getActiveKhachHang();
            long inactive = khachHangService.getInactiveKhachHang();
            
            return ResponseEntity.ok(new StatsResponse(total, active, inactive));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }


    // Tạo khách hàng mẫu
    @PostMapping("/create-sample")
    public ResponseEntity<?> createSampleKhachHang() {
        try {
            // Tạo khách hàng mẫu 1
            KhachHangDTO khachHang1 = KhachHangDTO.builder()
                    .tenKhachHang("Nguyễn Văn An")
                    .email("an@email.com")
                    .soDienThoai("0123456789")
                    .diaChi("123 Đường ABC, Quận 1, TP.HCM")
                    .ngaySinh(java.time.LocalDate.of(1990, 1, 15))
                    .gioiTinh(true)

                    .trangThai(true)
                    .build();

            // Tạo khách hàng mẫu 2
            KhachHangDTO khachHang2 = KhachHangDTO.builder()
                    .tenKhachHang("Trần Thị Bình")
                    .email("binh@email.com")
                    .soDienThoai("0987654321")
                    .diaChi("456 Đường XYZ, Quận 2, TP.HCM")
                    .ngaySinh(java.time.LocalDate.of(1985, 5, 20))
                    .gioiTinh(false)

                    .trangThai(true)
                    .build();

            KhachHangDTO created1 = khachHangService.createKhachHang(khachHang1);
            KhachHangDTO created2 = khachHangService.createKhachHang(khachHang2);

            return ResponseEntity.ok("Đã tạo 2 khách hàng mẫu thành công: " + 
                                   created1.getMaKhachHang() + " và " + created2.getMaKhachHang());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi tạo khách hàng mẫu: " + e.getMessage());
        }
    }

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API Khách hàng hoạt động bình thường!");
    }
    
    // Test database connection
    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabase() {
        try {
            long count = khachHangService.getTotalCustomerCount();
            return ResponseEntity.ok("Database connection OK. Total customers: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Database error: " + e.getMessage());
        }
    }
    
    // Test simple endpoint without database
    @GetMapping("/test-simple")
    public ResponseEntity<String> testSimple() {
        return ResponseEntity.ok("Simple test endpoint working!");
    }

    // Inner class cho response thống kê
public static class StatsResponse {
        private long total;
        private long active;
        private long inactive;
public StatsResponse(long total, long active, long inactive) {
            this.total = total;
            this.active = active;
            this.inactive = inactive;
        }

        // Getters
        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getInactive() { return inactive; }
    }
}
