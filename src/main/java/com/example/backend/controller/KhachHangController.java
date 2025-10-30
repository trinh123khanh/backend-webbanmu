package com.example.backend.controller;

import com.example.backend.dto.KhachHangDTO;
import com.example.backend.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*")
package com.example.backend.controller;

import com.example.backend.dto.KhachHangDTO;
import com.example.backend.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

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

    // Lấy khách hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<KhachHangDTO> getKhachHangById(@PathVariable Long id) {
        try {
            Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangById(id);
            return khachHang.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
package com.example.backend.controller;

import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.service.DiaChiKhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi-khach-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiaChiKhachHangController {
    
    private final DiaChiKhachHangService diaChiKhachHangService;
    
    // Lấy tất cả địa chỉ của khách hàng
    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<DiaChiKhachHangDTO>> getDiaChiByKhachHangId(@PathVariable Long khachHangId) {
        try {
            List<DiaChiKhachHangDTO> diaChiList = diaChiKhachHangService.getDiaChiByKhachHangId(khachHangId);
            return ResponseEntity.ok(diaChiList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Lấy địa chỉ mặc định của khách hàng
    @GetMapping("/khach-hang/{khachHangId}/mac-dinh")
    public ResponseEntity<DiaChiKhachHangDTO> getDiaChiMacDinhByKhachHangId(@PathVariable Long khachHangId) {
        try {
            return diaChiKhachHangService.getDiaChiMacDinhByKhachHangId(khachHangId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<DiaChiKhachHangDTO> createDiaChi(@RequestBody DiaChiKhachHangDTO diaChiDTO) {
        try {
            DiaChiKhachHangDTO savedDiaChi = diaChiKhachHangService.createDiaChi(diaChiDTO);
            return ResponseEntity.ok(savedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<DiaChiKhachHangDTO> updateDiaChi(@PathVariable Long id, @RequestBody DiaChiKhachHangDTO diaChiDTO) {
        try {
            DiaChiKhachHangDTO updatedDiaChi = diaChiKhachHangService.updateDiaChi(id, diaChiDTO);
            return ResponseEntity.ok(updatedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Xóa địa chỉ
    @DeleteMapping("/{id}/khach-hang/{khachHangId}")
    public ResponseEntity<Void> deleteDiaChi(@PathVariable Long id, @PathVariable Long khachHangId) {
        try {
            diaChiKhachHangService.deleteDiaChi(id, khachHangId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Đặt địa chỉ làm mặc định
    @PutMapping("/{id}/khach-hang/{khachHangId}/mac-dinh")
public ResponseEntity<DiaChiKhachHangDTO> setDiaChiMacDinh(@PathVariable Long id, @PathVariable Long khachHangId) {
        try {
            DiaChiKhachHangDTO updatedDiaChi = diaChiKhachHangService.setDiaChiMacDinh(id, khachHangId);
            return ResponseEntity.ok(updatedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Lấy tất cả địa chỉ (cho hiển thị bảng)
    @GetMapping
    public ResponseEntity<List<DiaChiKhachHangDTO>> getAllDiaChi() {
        try {
            List<DiaChiKhachHangDTO> allDiaChi = diaChiKhachHangService.getAllDiaChi();
            return ResponseEntity.ok(allDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}