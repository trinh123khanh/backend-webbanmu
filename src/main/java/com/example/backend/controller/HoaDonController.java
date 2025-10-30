package com.example.backend.controller;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.service.HoaDonService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class HoaDonController {

    private final HoaDonService hoaDonService;

    public HoaDonController(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getAllHoaDonPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String maHoaDon,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String trangThaiThanhToan,
            @RequestParam(required = false) String phuongThucThanhToan,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        try {
            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), 
                               sortBy != null ? sortBy : "ngayTao");
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Gọi service trả về Page<HoaDon> và tự map sang DTO
            Page<com.example.backend.entity.HoaDon> hoaDonPageEntity = hoaDonService.getAllHoaDon(keyword, pageable);
            Page<HoaDonDTO> hoaDonPage = hoaDonPageEntity.map(h -> HoaDonDTO.builder()
                    .id(h.getId())
                    .maHoaDon(h.getMaHoaDon())
                    .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                    .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                    .ngayTao(h.getNgayTao())
                    .ngayThanhToan(h.getNgayThanhToan())
                    .tongTien(h.getTongTien())
                    .tienGiamGia(h.getTienGiamGia())
                    .giamGiaPhanTram(h.getGiamGiaPhanTram())
                    .thanhTien(h.getThanhTien())
                    .ghiChu(h.getGhiChu())
                    .trangThai(h.getTrangThai())
                    .soLuongSanPham(h.getSoLuongSanPham())
                    .build());
            
            // Create response map
            Map<String, Object> response = new HashMap<>();
            response.put("content", hoaDonPage.getContent());
            response.put("totalElements", hoaDonPage.getTotalElements());
            response.put("totalPages", hoaDonPage.getTotalPages());
            response.put("currentPage", hoaDonPage.getNumber());
            response.put("size", hoaDonPage.getSize());
            response.put("first", hoaDonPage.isFirst());
            response.put("last", hoaDonPage.isLast());
            response.put("numberOfElements", hoaDonPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy dữ liệu: " + e.getMessage());
            errorResponse.put("content", List.of());
            errorResponse.put("totalElements", 0);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getHoaDonById(@PathVariable Long id) {
        return hoaDonService.getHoaDonById(id)
                .map(h -> ResponseEntity.ok(HoaDonDTO.builder()
                        .id(h.getId())
                        .maHoaDon(h.getMaHoaDon())
                        .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                        .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                        .ngayTao(h.getNgayTao())
                        .ngayThanhToan(h.getNgayThanhToan())
                        .tongTien(h.getTongTien())
                        .tienGiamGia(h.getTienGiamGia())
                        .giamGiaPhanTram(h.getGiamGiaPhanTram())
                        .thanhTien(h.getThanhTien())
                        .ghiChu(h.getGhiChu())
                        .trangThai(h.getTrangThai())
                        .soLuongSanPham(h.getSoLuongSanPham())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạm thời ẩn các endpoint tạo/sửa/xóa để đảm bảo build ổn định

    @PostMapping("/create-sample-data")
    public ResponseEntity<String> createSampleData() {
        try {
            // Tạo dữ liệu mẫu
            HoaDonDTO sample1 = HoaDonDTO.builder()
                    .maHoaDon("HD-TEST-001")
                    .khachHangId(1L)
                    .tenKhachHang("Nguyễn Văn An")
                    .emailKhachHang("an@email.com")
                    .soDienThoaiKhachHang("0123456789")
                    .nhanVienId(1L)
                    .tenNhanVien("Nguyễn Văn A")
                    .tongTien(java.math.BigDecimal.valueOf(1000000))
                    .thanhTien(java.math.BigDecimal.valueOf(1000000))
                    .trangThai(HoaDon.TrangThaiHoaDon.CHO_XAC_NHAN)
                    .ngayTao(java.time.LocalDateTime.now())
                    .build();

            HoaDonDTO sample2 = HoaDonDTO.builder()
                    .maHoaDon("HD-TEST-002")
                    .khachHangId(2L)
                    .tenKhachHang("Trần Thị Bình")
                    .emailKhachHang("binh@email.com")
                    .soDienThoaiKhachHang("0987654321")
                    .nhanVienId(2L)
                    .tenNhanVien("Trần Thị B")
                    .tongTien(java.math.BigDecimal.valueOf(2000000))
                    .thanhTien(java.math.BigDecimal.valueOf(1800000))
                    .tienGiamGia(java.math.BigDecimal.valueOf(200000))
                    .trangThai(HoaDon.TrangThaiHoaDon.DA_XAC_NHAN)
                    .ngayTao(java.time.LocalDateTime.now().minusHours(2))
                    .build();

            hoaDonService.createHoaDon(sample1);
            hoaDonService.createHoaDon(sample2);

            return ResponseEntity.ok("Đã tạo 2 hóa đơn mẫu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }

    // Removed createSampleCustomers method as KhachHangRepository was deleted

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API hoạt động bình thường!");
    }
}
