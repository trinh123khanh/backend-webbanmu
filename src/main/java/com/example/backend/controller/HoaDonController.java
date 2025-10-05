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
    
    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<HoaDonDTO>> getHoaDonByKhachHang(@PathVariable Long khachHangId) {
        List<HoaDonDTO> hoaDonList = hoaDonService.getHoaDonByKhachHang(khachHangId);
        return ResponseEntity.ok(hoaDonList);
    }
    
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

    @PostMapping("/create-sample-customers")
    public ResponseEntity<String> createSampleCustomers() {
        try {
            // Tạo khách hàng mẫu
            com.example.backend.entity.KhachHang khachHang1 = new com.example.backend.entity.KhachHang();
            khachHang1.setTenKhachHang("Nguyễn Văn An");
            khachHang1.setEmail("an@email.com");
            khachHang1.setSoDienThoai("0123456789");
            khachHang1.setNgaySinh(java.time.LocalDate.of(1990, 1, 15));
            khachHang1.setGioiTinh(true);
            khachHang1.setDiemTichLuy(100);
            khachHang1.setNgayTao(java.time.LocalDate.now());
            khachHang1.setTrangThai(true);

            com.example.backend.entity.KhachHang khachHang2 = new com.example.backend.entity.KhachHang();
            khachHang2.setTenKhachHang("Trần Thị Bình");
            khachHang2.setEmail("binh@email.com");
            khachHang2.setSoDienThoai("0987654321");
            khachHang2.setNgaySinh(java.time.LocalDate.of(1985, 5, 20));
            khachHang2.setGioiTinh(false);
            khachHang2.setDiemTichLuy(200);
            khachHang2.setNgayTao(java.time.LocalDate.now());
            khachHang2.setTrangThai(true);

            // Tạo nhân viên mẫu
            com.example.backend.entity.NhanVien nhanVien1 = new com.example.backend.entity.NhanVien();
            nhanVien1.setHoTen("Nguyễn Văn A");
            nhanVien1.setEmail("nva@company.com");
            nhanVien1.setSoDienThoai("0123456780");
            nhanVien1.setDiaChi("Hà Nội");
            nhanVien1.setGioiTinh(true);
            nhanVien1.setNgaySinh(java.time.LocalDate.of(1988, 3, 15));
            nhanVien1.setNgayVaoLam(java.time.LocalDate.of(2020, 1, 1));
            nhanVien1.setTrangThai(true);

            com.example.backend.entity.NhanVien nhanVien2 = new com.example.backend.entity.NhanVien();
            nhanVien2.setHoTen("Trần Thị B");
            nhanVien2.setEmail("ttb@company.com");
            nhanVien2.setSoDienThoai("0987654320");
            nhanVien2.setDiaChi("TP.HCM");
            nhanVien2.setGioiTinh(false);
            nhanVien2.setNgaySinh(java.time.LocalDate.of(1990, 7, 22));
            nhanVien2.setNgayVaoLam(java.time.LocalDate.of(2020, 2, 1));
            nhanVien2.setTrangThai(true);

            // Lưu vào database
            hoaDonService.getKhachHangRepository().save(khachHang1);
            hoaDonService.getKhachHangRepository().save(khachHang2);
            hoaDonService.getNhanVienRepository().save(nhanVien1);
            hoaDonService.getNhanVienRepository().save(nhanVien2);

            return ResponseEntity.ok("Đã tạo khách hàng và nhân viên mẫu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API hoạt động bình thường!");
    }
}
