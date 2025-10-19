package com.example.backend.controller;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.service.HoaDonService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class HoaDonController {

    private final HoaDonService hoaDonService;

    public HoaDonController(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    @GetMapping
    public ResponseEntity<List<HoaDonDTO>> getAllHoaDon() {
        List<HoaDonDTO> hoaDonList = hoaDonService.getAllHoaDon();
        return ResponseEntity.ok(hoaDonList);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getHoaDonById(@PathVariable Long id) {
        HoaDonDTO hoaDon = hoaDonService.getHoaDonById(id);
        return ResponseEntity.ok(hoaDon);
    }
    
    @GetMapping("/ma/{maHoaDon}")
    public ResponseEntity<HoaDonDTO> getHoaDonByMa(@PathVariable String maHoaDon) {
        HoaDonDTO hoaDon = hoaDonService.getHoaDonByMa(maHoaDon);
        return ResponseEntity.ok(hoaDon);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<HoaDonDTO>> getHoaDonByTrangThai(
            @PathVariable HoaDon.TrangThaiHoaDon trangThai) {
        List<HoaDonDTO> hoaDonList = hoaDonService.getHoaDonByTrangThai(trangThai);
        return ResponseEntity.ok(hoaDonList);
    }
    
    // Removed getHoaDonByKhachHang endpoint as KhachHangRepository was deleted
    
    @GetMapping("/nhan-vien/{nhanVienId}")
    public ResponseEntity<List<HoaDonDTO>> getHoaDonByNhanVien(@PathVariable Long nhanVienId) {
        List<HoaDonDTO> hoaDonList = hoaDonService.getHoaDonByNhanVien(nhanVienId);
        return ResponseEntity.ok(hoaDonList);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<HoaDonDTO>> getHoaDonByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<HoaDonDTO> hoaDonList = hoaDonService.getHoaDonByDateRange(startDate, endDate);
        return ResponseEntity.ok(hoaDonList);
    }

    @PostMapping
    public ResponseEntity<HoaDonDTO> createHoaDon(@RequestBody HoaDonDTO hoaDonDTO) {
        HoaDonDTO createdHoaDon = hoaDonService.createHoaDon(hoaDonDTO);
        return ResponseEntity.ok(createdHoaDon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoaDonDTO> updateHoaDon(@PathVariable Long id, @RequestBody HoaDonDTO hoaDonDTO) {
        HoaDonDTO updatedHoaDon = hoaDonService.updateHoaDon(id, hoaDonDTO);
        return ResponseEntity.ok(updatedHoaDon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoaDon(@PathVariable Long id) {
        hoaDonService.deleteHoaDon(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<HoaDonDTO> updateTrangThaiHoaDon(
            @PathVariable Long id, 
            @RequestParam HoaDon.TrangThaiHoaDon trangThai) {
        HoaDonDTO updatedHoaDon = hoaDonService.updateTrangThaiHoaDon(id, trangThai);
        return ResponseEntity.ok(updatedHoaDon);
    }

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
