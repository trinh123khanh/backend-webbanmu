package com.example.backend.controller;

import com.example.backend.dto.KhachHangDTO;
import com.example.backend.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangService khachHangService;

    // Lấy tất cả khách hàng với phân trang
    @GetMapping
    public ResponseEntity<Page<KhachHangDTO>> getAllKhachHang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<KhachHangDTO> khachHangPage = khachHangService.getAllKhachHang(page, size, sortBy, sortDir);
        return ResponseEntity.ok(khachHangPage);
    }

    // Lấy khách hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<KhachHangDTO> getKhachHangById(@PathVariable Long id) {
        Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangById(id);
        return khachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tìm kiếm khách hàng với bộ lọc
    @GetMapping("/search")
    public ResponseEntity<Page<KhachHangDTO>> searchKhachHang(
            @RequestParam(required = false) String maKhachHang,
            @RequestParam(required = false) String tenKhachHang,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String soDienThoai,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<KhachHangDTO> khachHangPage = khachHangService.searchKhachHang(
            maKhachHang, tenKhachHang, email, soDienThoai, trangThai, page, size, sortBy, sortDir);
        return ResponseEntity.ok(khachHangPage);
    }

    // Tạo khách hàng mới
    @PostMapping
    public ResponseEntity<KhachHangDTO> createKhachHang(@RequestBody KhachHangDTO khachHangDTO) {
        try {
            // Kiểm tra email đã tồn tại
            if (khachHangDTO.getEmail() != null && khachHangService.existsByEmail(khachHangDTO.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra số điện thoại đã tồn tại
            if (khachHangDTO.getSoDienThoai() != null && khachHangService.existsBySoDienThoai(khachHangDTO.getSoDienThoai())) {
                return ResponseEntity.badRequest().build();
            }
            
            KhachHangDTO createdKhachHang = khachHangService.createKhachHang(khachHangDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdKhachHang);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cập nhật khách hàng
    @PutMapping("/{id}")
    public ResponseEntity<KhachHangDTO> updateKhachHang(@PathVariable Long id, @RequestBody KhachHangDTO khachHangDTO) {
        Optional<KhachHangDTO> updatedKhachHang = khachHangService.updateKhachHang(id, khachHangDTO);
        return updatedKhachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa khách hàng (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKhachHang(@PathVariable Long id) {
        boolean deleted = khachHangService.deleteKhachHang(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Xóa vĩnh viễn khách hàng
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteKhachHang(@PathVariable Long id) {
        boolean deleted = khachHangService.permanentlyDeleteKhachHang(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Lấy khách hàng theo email
    @GetMapping("/email/{email}")
    public ResponseEntity<KhachHangDTO> getKhachHangByEmail(@PathVariable String email) {
        Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangByEmail(email);
        return khachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy khách hàng theo số điện thoại
    @GetMapping("/phone/{soDienThoai}")
    public ResponseEntity<KhachHangDTO> getKhachHangBySoDienThoai(@PathVariable String soDienThoai) {
        Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangBySoDienThoai(soDienThoai);
        return khachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy khách hàng theo mã khách hàng
    @GetMapping("/code/{maKhachHang}")
    public ResponseEntity<KhachHangDTO> getKhachHangByMaKhachHang(@PathVariable String maKhachHang) {
        Optional<KhachHangDTO> khachHang = khachHangService.getKhachHangByMaKhachHang(maKhachHang);
        return khachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy khách hàng VIP (điểm tích lũy cao)
    @GetMapping("/vip")
    public ResponseEntity<List<KhachHangDTO>> getKhachHangVIP(@RequestParam(defaultValue = "10") int limit) {
        List<KhachHangDTO> vipKhachHang = khachHangService.getKhachHangVIP(limit);
        return ResponseEntity.ok(vipKhachHang);
    }

    // Thống kê khách hàng
    @GetMapping("/stats")
    public ResponseEntity<Object> getKhachHangStats() {
        long totalActive = khachHangService.countKhachHangByTrangThai(true);
        long totalInactive = khachHangService.countKhachHangByTrangThai(false);
        
        return ResponseEntity.ok(new Object() {
            public final long activeCount = totalActive;
            public final long inactiveCount = totalInactive;
            public final long totalCount = totalActive + totalInactive;
        });
    }

    // Lấy khách hàng theo khoảng thời gian
    @GetMapping("/date-range")
    public ResponseEntity<List<KhachHangDTO>> getKhachHangByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<KhachHangDTO> khachHangList = khachHangService.getKhachHangByDateRange(start, end);
            return ResponseEntity.ok(khachHangList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cập nhật điểm tích lũy
    @PatchMapping("/{id}/diem-tich-luy")
    public ResponseEntity<KhachHangDTO> updateDiemTichLuy(@PathVariable Long id, @RequestBody Integer diemTichLuy) {
        Optional<KhachHangDTO> updatedKhachHang = khachHangService.updateDiemTichLuy(id, diemTichLuy);
        return updatedKhachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Kiểm tra email đã tồn tại
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Object> checkEmailExists(@PathVariable String email) {
        boolean emailExists = khachHangService.existsByEmail(email);
        return ResponseEntity.ok(new Object() {
            public final boolean exists = emailExists;
        });
    }

    // Kiểm tra số điện thoại đã tồn tại
    @GetMapping("/check-phone/{soDienThoai}")
    public ResponseEntity<Object> checkPhoneExists(@PathVariable String soDienThoai) {
        boolean phoneExists = khachHangService.existsBySoDienThoai(soDienThoai);
        return ResponseEntity.ok(new Object() {
            public final boolean exists = phoneExists;
        });
    }

    // Kiểm tra mã khách hàng đã tồn tại
    @GetMapping("/check-code/{maKhachHang}")
    public ResponseEntity<Object> checkMaKhachHangExists(@PathVariable String maKhachHang) {
        boolean codeExists = khachHangService.existsByMaKhachHang(maKhachHang);
        return ResponseEntity.ok(new Object() {
            public final boolean exists = codeExists;
        });
    }
    
    // Lấy danh sách khách hàng cho form phiếu giảm giá (endpoint đặc biệt)
    @GetMapping("/for-voucher")
    public ResponseEntity<List<KhachHangDTO>> getAllCustomersForVoucher() {
        List<KhachHangDTO> customers = khachHangService.getAllCustomersForVoucher();
        return ResponseEntity.ok(customers);
    }
}
