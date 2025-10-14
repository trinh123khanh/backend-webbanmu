package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.KhachHangRequest;
import com.example.backend.dto.KhachHangResponse;
import com.example.backend.dto.DiaChiKhachHangRequest;
import com.example.backend.dto.DiaChiKhachHangResponse;
import com.example.backend.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    // Khách hàng endpoints
    @GetMapping
    public ResponseEntity<ApiResponse<List<KhachHangResponse>>> getAllKhachHang() {
        try {
            List<KhachHangResponse> khachHangList = khachHangService.getAllKhachHang();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng thành công", khachHangList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách khách hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KhachHangResponse>> getKhachHangById(@PathVariable Long id) {
        try {
            KhachHangResponse khachHang = khachHangService.getKhachHangById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", khachHang));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin khách hàng: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KhachHangResponse>> createKhachHang(@RequestBody KhachHangRequest request) {
        try {
            KhachHangResponse khachHang = khachHangService.createKhachHang(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo khách hàng thành công", khachHang));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo khách hàng: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KhachHangResponse>> updateKhachHang(@PathVariable Long id, @RequestBody KhachHangRequest request) {
        try {
            KhachHangResponse khachHang = khachHangService.updateKhachHang(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật khách hàng thành công", khachHang));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật khách hàng: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKhachHang(@PathVariable Long id) {
        try {
            khachHangService.deleteKhachHang(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa khách hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KhachHangResponse>>> searchKhachHang(@RequestParam String keyword) {
        try {
            List<KhachHangResponse> khachHangList = khachHangService.searchKhachHang(keyword);
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khách hàng thành công", khachHangList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tìm kiếm khách hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<ApiResponse<List<KhachHangResponse>>> getKhachHangByTrangThai(@PathVariable Boolean trangThai) {
        try {
            List<KhachHangResponse> khachHangList = khachHangService.getKhachHangByTrangThai(trangThai);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng theo trạng thái thành công", khachHangList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách khách hàng theo trạng thái: " + e.getMessage()));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<KhachHangResponse>> getKhachHangByEmail(@PathVariable String email) {
        try {
            KhachHangResponse khachHang = khachHangService.getKhachHangByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng theo email thành công", khachHang));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin khách hàng theo email: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<KhachHangResponse>> getKhachHangByUserId(@PathVariable Long userId) {
        try {
            KhachHangResponse khachHang = khachHangService.getKhachHangByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng theo user ID thành công", khachHang));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin khách hàng theo user ID: " + e.getMessage()));
        }
    }

    @GetMapping("/top-diem-tich-luy")
    public ResponseEntity<ApiResponse<List<KhachHangResponse>>> getTopKhachHangByDiemTichLuy() {
        try {
            List<KhachHangResponse> khachHangList = khachHangService.getTopKhachHangByDiemTichLuy();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng theo điểm tích lũy thành công", khachHangList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách khách hàng theo điểm tích lũy: " + e.getMessage()));
        }
    }

    // Địa chỉ khách hàng endpoints
    @GetMapping("/{khachHangId}/dia-chi")
    public ResponseEntity<ApiResponse<List<DiaChiKhachHangResponse>>> getDiaChiByKhachHangId(@PathVariable Long khachHangId) {
        try {
            List<DiaChiKhachHangResponse> diaChiList = khachHangService.getDiaChiByKhachHangId(khachHangId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách địa chỉ khách hàng thành công", diaChiList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách địa chỉ khách hàng: " + e.getMessage()));
        }
    }

    @PostMapping("/dia-chi")
    public ResponseEntity<ApiResponse<DiaChiKhachHangResponse>> createDiaChi(@RequestBody DiaChiKhachHangRequest request) {
        try {
            DiaChiKhachHangResponse diaChi = khachHangService.createDiaChi(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo địa chỉ khách hàng thành công", diaChi));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo địa chỉ khách hàng: " + e.getMessage()));
        }
    }

    @PutMapping("/dia-chi/{id}")
    public ResponseEntity<ApiResponse<DiaChiKhachHangResponse>> updateDiaChi(@PathVariable Long id, @RequestBody DiaChiKhachHangRequest request) {
        try {
            DiaChiKhachHangResponse diaChi = khachHangService.updateDiaChi(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ khách hàng thành công", diaChi));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật địa chỉ khách hàng: " + e.getMessage()));
        }
    }

    @DeleteMapping("/dia-chi/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiaChi(@PathVariable Long id) {
        try {
            khachHangService.deleteDiaChi(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ khách hàng thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa địa chỉ khách hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/dia-chi/{id}")
    public ResponseEntity<ApiResponse<DiaChiKhachHangResponse>> getDiaChiById(@PathVariable Long id) {
        try {
            DiaChiKhachHangResponse diaChi = khachHangService.getDiaChiById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin địa chỉ khách hàng thành công", diaChi));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin địa chỉ khách hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/{khachHangId}/dia-chi/mac-dinh")
    public ResponseEntity<ApiResponse<List<DiaChiKhachHangResponse>>> getDiaChiMacDinhByKhachHangId(@PathVariable Long khachHangId) {
        try {
            List<DiaChiKhachHangResponse> diaChiList = khachHangService.getDiaChiMacDinhByKhachHangId(khachHangId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách địa chỉ mặc định khách hàng thành công", diaChiList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách địa chỉ mặc định khách hàng: " + e.getMessage()));
        }
    }

    @PutMapping("/dia-chi/{id}/mac-dinh")
    public ResponseEntity<ApiResponse<Void>> setDiaChiMacDinh(@PathVariable Long id, @RequestParam Long khachHangId) {
        try {
            khachHangService.setDiaChiMacDinh(id, khachHangId);
            return ResponseEntity.ok(ApiResponse.success("Đặt địa chỉ mặc định thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi đặt địa chỉ mặc định: " + e.getMessage()));
        }
    }
}
