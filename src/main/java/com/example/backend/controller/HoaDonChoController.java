package com.example.backend.controller;

import com.example.backend.dto.HoaDonChoRequest;
import com.example.backend.dto.HoaDonChoResponse;
import com.example.backend.service.HoaDonChoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don-cho")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class HoaDonChoController {

    private final HoaDonChoService hoaDonChoService;

    public HoaDonChoController(HoaDonChoService hoaDonChoService) {
        this.hoaDonChoService = hoaDonChoService;
    }

    @GetMapping
    public ResponseEntity<List<HoaDonChoResponse>> getAllHoaDonCho() {
        try {
            List<HoaDonChoResponse> hoaDonChoList = hoaDonChoService.getAllHoaDonCho();
            return ResponseEntity.ok(hoaDonChoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaDonChoResponse> getHoaDonChoById(@PathVariable Long id) {
        try {
            HoaDonChoResponse response = hoaDonChoService.getHoaDonChoById(id);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<HoaDonChoResponse>> getHoaDonChoByTrangThai(@PathVariable String trangThai) {
        try {
            List<HoaDonChoResponse> hoaDonChoList = hoaDonChoService.getHoaDonChoByTrangThai(trangThai);
            return ResponseEntity.ok(hoaDonChoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<HoaDonChoResponse>> getHoaDonChoByKhachHangId(@PathVariable Long khachHangId) {
        try {
            List<HoaDonChoResponse> hoaDonChoList = hoaDonChoService.getHoaDonChoByKhachHangId(khachHangId);
            return ResponseEntity.ok(hoaDonChoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createHoaDonCho(@RequestBody HoaDonChoRequest request) {
        try {
            System.out.println("📥 POST /api/hoa-don-cho - Creating cart");
            System.out.println("   - maHoaDonCho: " + request.getMaHoaDonCho());
            System.out.println("   - khachHangId: " + request.getKhachHangId());
            System.out.println("   - trangThai: " + request.getTrangThai());
            
            if (request.getMaHoaDonCho() == null || request.getMaHoaDonCho().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mã hóa đơn chờ không được để trống");
                System.out.println("❌ Validation failed: Mã hóa đơn chờ không được để trống");
                return ResponseEntity.badRequest().body(error);
            }
            HoaDonChoResponse response = hoaDonChoService.createHoaDonCho(request);
            System.out.println("✅ Cart created successfully with ID: " + response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("❌ Error creating cart: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi tạo hóa đơn chờ: " + e.getMessage());
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHoaDonCho(@PathVariable Long id, @RequestBody HoaDonChoRequest request) {
        try {
            HoaDonChoResponse response = hoaDonChoService.updateHoaDonCho(id, request);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi cập nhật hóa đơn chờ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHoaDonCho(@PathVariable Long id) {
        try {
            hoaDonChoService.deleteHoaDonCho(id);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi xóa hóa đơn chờ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{hoaDonChoId}/gio-hang")
    public ResponseEntity<?> addItemToCart(@PathVariable Long hoaDonChoId, @RequestBody com.example.backend.dto.GioHangChoItemRequest itemRequest) {
        try {
            HoaDonChoResponse response = hoaDonChoService.addItemToCart(hoaDonChoId, itemRequest);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{hoaDonChoId}/gio-hang/{gioHangChoId}/so-luong")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long hoaDonChoId,
            @PathVariable Long gioHangChoId,
            @RequestParam Integer soLuong) {
        try {
            HoaDonChoResponse response = hoaDonChoService.updateCartItemQuantity(hoaDonChoId, gioHangChoId, soLuong);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi cập nhật số lượng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{hoaDonChoId}/gio-hang/{gioHangChoId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long hoaDonChoId, @PathVariable Long gioHangChoId) {
        try {
            HoaDonChoResponse response = hoaDonChoService.removeItemFromCart(hoaDonChoId, gioHangChoId);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi xóa sản phẩm khỏi giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

