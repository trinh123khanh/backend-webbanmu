package com.example.backend.controller;

import com.example.backend.dto.HoaDonActivityDTO;
import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.User;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.HoaDonService;
import com.example.backend.service.HoaDonActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final UserRepository userRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonActivityService hoaDonActivityService;

    public HoaDonController(HoaDonService hoaDonService,
                            UserRepository userRepository,
                            KhachHangRepository khachHangRepository,
                            HoaDonActivityService hoaDonActivityService) {
        this.hoaDonService = hoaDonService;
        this.userRepository = userRepository;
        this.khachHangRepository = khachHangRepository;
        this.hoaDonActivityService = hoaDonActivityService;
    }

    // ===== ADMIN ENDPOINTS - CRUD tất cả hóa đơn =====
    @GetMapping("/api/admin/invoices/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllHoaDonPaginatedForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String maHoaDon,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String trangThaiThanhToan,
            @RequestParam(required = false) String phuongThucThanhToan,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return getAllHoaDonPaginated(page, size, maHoaDon, keyword, trangThai, trangThaiThanhToan, phuongThucThanhToan, sortBy, sortDirection);
    }

    @GetMapping("/api/admin/invoices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HoaDonDTO> getHoaDonByIdForAdmin(@PathVariable Long id) {
        return getHoaDonById(id);
    }

    @PostMapping("/api/admin/invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createHoaDonForAdmin(@RequestBody HoaDonDTO hoaDonDTO) {
        return createHoaDon(hoaDonDTO);
    }

    @PutMapping("/api/admin/invoices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateHoaDonForAdmin(@PathVariable Long id, @RequestBody HoaDonDTO hoaDonDTO) {
        return updateHoaDon(id, hoaDonDTO);
    }

    @PatchMapping("/api/admin/invoices/{id}/trang-thai")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTrangThaiHoaDonForAdmin(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        return updateTrangThaiHoaDon(id, requestBody);
    }

    // ===== STAFF ENDPOINTS - CRUD hóa đơn do mình tạo =====
    @GetMapping("/api/staff/invoices/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Object>> getAllHoaDonPaginatedForStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String maHoaDon,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String trangThaiThanhToan,
            @RequestParam(required = false) String phuongThucThanhToan,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        // TODO: Filter chỉ hóa đơn do nhân viên này tạo
        // Hiện tại trả về tất cả, cần thêm logic filter theo nhanVienId từ authentication
        return getAllHoaDonPaginated(page, size, maHoaDon, keyword, trangThai, trangThaiThanhToan, phuongThucThanhToan, sortBy, sortDirection);
    }

    @PostMapping("/api/staff/sell")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> createHoaDonForStaff(@RequestBody HoaDonDTO hoaDonDTO) {
        // Set nhanVienId từ authentication context
        // TODO: Lấy nhanVienId từ user context
        return createHoaDon(hoaDonDTO);
    }

    @PatchMapping("/api/staff/invoices/{id}/trang-thai")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateTrangThaiHoaDonForStaff(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        return updateTrangThaiHoaDon(id, requestBody);
    }

    // ===== CUSTOMER ENDPOINTS - Xem/hủy đơn hàng =====
    @GetMapping("/api/customer/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getOrdersForCustomer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Lấy username từ authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Tìm User từ username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Tìm KhachHang từ userId
            KhachHang khachHang = khachHangRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("KhachHang not found for user: " + username));
            
            // Gọi service để lấy đơn hàng của khách hàng
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
            Page<HoaDon> hoaDonPage = hoaDonService.getHoaDonByKhachHangId(khachHang.getId(), pageable);
            Page<HoaDonDTO> hoaDonDTOPage = hoaDonPage.map(hoaDonService::toDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", hoaDonDTOPage.getContent());
            response.put("totalElements", hoaDonDTOPage.getTotalElements());
            response.put("totalPages", hoaDonDTOPage.getTotalPages());
            response.put("currentPage", hoaDonDTOPage.getNumber());
            response.put("size", hoaDonDTOPage.getSize());
            response.put("first", hoaDonDTOPage.isFirst());
            response.put("last", hoaDonDTOPage.isLast());
            response.put("numberOfElements", hoaDonDTOPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy đơn hàng: " + e.getMessage());
            errorResponse.put("content", List.of());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("size", size);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PatchMapping("/api/customer/orders/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> cancelOrderForCustomer(@PathVariable Long id) {
        // Chỉ cho phép hủy đơn hàng ở trạng thái CHO_XAC_NHAN
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("trangThai", "HUY");
        return updateTrangThaiHoaDon(id, requestBody);
    }

    // ===== BACKWARD COMPATIBILITY - Giữ lại các endpoint cũ =====
    @GetMapping("/api/hoa-don/page")
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
            // Chỉ sort nếu có sortBy được chỉ định, nếu không thì không sort (để data hiển thị theo thứ tự tự nhiên)
            Pageable pageable;
            if (sortBy != null && !sortBy.trim().isEmpty()) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
                pageable = PageRequest.of(page, size, sort);
            } else {
                // Không sort - để data hiển thị theo thứ tự tự nhiên (mới nhất ở cuối)
                pageable = PageRequest.of(page, size);
            }
            
            // Gọi service trả về Page<HoaDon> và map sang DTO với đầy đủ thông tin
            Page<com.example.backend.entity.HoaDon> hoaDonPageEntity = hoaDonService.getAllHoaDon(keyword, phuongThucThanhToan, trangThai, pageable);
            Page<HoaDonDTO> hoaDonPage = hoaDonPageEntity.map(hoaDonService::toDTO);
            
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
    
    @GetMapping("/api/hoa-don/{id:\\d+}")
    public ResponseEntity<HoaDonDTO> getHoaDonById(@PathVariable Long id) {
        return hoaDonService.getHoaDonById(id)
                .map(hoaDonService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo hóa đơn mới
    @PostMapping("/api/hoa-don")
    public ResponseEntity<?> createHoaDon(@RequestBody HoaDonDTO hoaDonDTO) {
        try {
            // Validate dữ liệu đầu vào
            if (hoaDonDTO.getMaHoaDon() == null || hoaDonDTO.getMaHoaDon().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mã hóa đơn không được để trống");
            }
            // CHO PHÉP khachHangId = null để test thanh toán không cần đăng nhập
            // if (hoaDonDTO.getKhachHangId() == null) {
            //     return ResponseEntity.badRequest().body("Khách hàng ID không được để trống");
            // }
            if (hoaDonDTO.getTongTien() == null || hoaDonDTO.getTongTien().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Tổng tiền phải lớn hơn 0");
            }
            
            HoaDonDTO createdHoaDon = hoaDonService.createHoaDon(hoaDonDTO);
            hoaDonActivityService.logActivity(
                    createdHoaDon.getId(),
                    createdHoaDon.getMaHoaDon(),
                    "CREATE",
                    "Tạo hóa đơn mới trong hệ thống"
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdHoaDon);
        } catch (RuntimeException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server: " + e.getMessage());
        }
    }

    // Cập nhật hóa đơn
    @PutMapping("/api/hoa-don/{id:\\d+}")
    public ResponseEntity<?> updateHoaDon(@PathVariable Long id, @RequestBody HoaDonDTO hoaDonDTO) {
        try {
            System.out.println("🔍 ========== PUT /api/hoa-don/" + id + " ==========");
            System.out.println("📥 Received HoaDonDTO:");
            System.out.println("   - maHoaDon: " + hoaDonDTO.getMaHoaDon());
            System.out.println("   - trangThai: " + hoaDonDTO.getTrangThai());
            System.out.println("   - ghiChu: " + hoaDonDTO.getGhiChu());
            System.out.println("   - ghiChu length: " + (hoaDonDTO.getGhiChu() != null ? hoaDonDTO.getGhiChu().length() : "null"));
            System.out.println("   - danhSachChiTiet size: " + (hoaDonDTO.getDanhSachChiTiet() != null ? hoaDonDTO.getDanhSachChiTiet().size() : "null"));
            
            // Validate dữ liệu đầu vào
            if (hoaDonDTO.getMaHoaDon() == null || hoaDonDTO.getMaHoaDon().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mã hóa đơn không được để trống");
            }
            if (hoaDonDTO.getKhachHangId() == null) {
                return ResponseEntity.badRequest().body("Khách hàng ID không được để trống");
            }
            if (hoaDonDTO.getTongTien() == null || hoaDonDTO.getTongTien().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Tổng tiền phải lớn hơn 0");
            }
            
            HoaDonDTO updatedHoaDon = hoaDonService.updateHoaDon(id, hoaDonDTO);
            hoaDonActivityService.logActivity(
                    updatedHoaDon.getId(),
                    updatedHoaDon.getMaHoaDon(),
                    "UPDATE",
                    "Cập nhật thông tin hóa đơn"
            );
            System.out.println("✅ Invoice updated successfully:");
            System.out.println("   - New status: " + updatedHoaDon.getTrangThai());
            System.out.println("   - New ghiChu: " + updatedHoaDon.getGhiChu());
            System.out.println("   - ghiChu length: " + (updatedHoaDon.getGhiChu() != null ? updatedHoaDon.getGhiChu().length() : "null"));
            System.out.println("==========================================");
            return ResponseEntity.ok(updatedHoaDon);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server: " + e.getMessage());
        }
    }

    // Tạm thời ẩn các endpoint tạo/sửa/xóa để đảm bảo build ổn định

    @PostMapping("/api/admin/invoices/create-sample-data")
    @PreAuthorize("hasRole('ADMIN')")
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
                    .trangThai("CHO_XAC_NHAN")
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
                    .trangThai("DA_XAC_NHAN")
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

    // Cập nhật trạng thái hóa đơn
    // Best Practice: PATCH request nên dùng @RequestBody (RFC 5789)
    // Ưu điểm: Dễ mở rộng (có thể thêm reason, note), dễ debug, consistent với REST standards
    @PatchMapping(value = "/api/hoa-don/{id:\\d+}/trang-thai", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateTrangThaiHoaDon(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            System.out.println("🔍 ========== PATCH /api/hoa-don/" + id + "/trang-thai ==========");
            System.out.println("📥 Received request body: " + requestBody);
            System.out.println("📥 Request body is null: " + (requestBody == null));
            
            // Kiểm tra requestBody null hoặc empty
            if (requestBody == null || requestBody.isEmpty()) {
                System.err.println("❌ Request body is null or empty");
                return ResponseEntity.badRequest()
                    .body("Required parameter 'trangThai' is not present. Please send { \"trangThai\": \"HUY\" } in request body.");
            }
            
            // Lấy trangThai từ request body
            String trangThai = requestBody.get("trangThai");
            if (trangThai == null || trangThai.trim().isEmpty()) {
                System.err.println("❌ trangThai is null or empty in request body");
                System.err.println("📥 Available keys in requestBody: " + requestBody.keySet());
                return ResponseEntity.badRequest()
                    .body("Required parameter 'trangThai' is not present. Please send { \"trangThai\": \"HUY\" } in request body.");
            }
            
            System.out.println("📥 Received trangThai from body: '" + trangThai + "'");
            
            // Map "HUY" từ frontend sang "DA_HUY" cho backend
            String trangThaiToUpdate = trangThai;
            if ("HUY".equals(trangThai)) {
                trangThaiToUpdate = "DA_HUY";
                System.out.println("🔄 Mapped HUY -> DA_HUY");
            }
            
            // Validate trạng thái
            try {
                HoaDon.TrangThaiHoaDon.valueOf(trangThaiToUpdate);
                System.out.println("✅ Valid trangThai: " + trangThaiToUpdate);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Invalid trangThai: " + trangThaiToUpdate);
                System.err.println("💡 Valid values: CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO_HANG, DA_GIAO_HANG, DA_HUY");
                return ResponseEntity.badRequest()
                    .body("Trạng thái không hợp lệ: " + trangThai + ". Giá trị hợp lệ: CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO_HANG, DA_GIAO_HANG, HUY");
            }
            
            System.out.println("📞 Calling service.updateTrangThaiHoaDon...");
            HoaDonDTO updatedHoaDon = hoaDonService.updateTrangThaiHoaDon(id, trangThaiToUpdate);
            System.out.println("✅ Update successful, new status: " + updatedHoaDon.getTrangThai());
            System.out.println("==========================================");
            hoaDonActivityService.logActivity(
                    updatedHoaDon.getId(),
                    updatedHoaDon.getMaHoaDon(),
                    "STATUS_CHANGE",
                    "Cập nhật trạng thái thành " + updatedHoaDon.getTrangThai()
            );
            return ResponseEntity.ok(updatedHoaDon);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            System.err.println("❌ Entity not found: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("❌ RuntimeException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/api/hoa-don/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API hoạt động bình thường!");
    }

    @GetMapping("/api/hoa-don/activities")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Map<String, Object>> getHoaDonActivities(
            @RequestParam(required = false) Long hoaDonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<HoaDonActivityDTO> activityPage = hoaDonActivityService.getActivities(hoaDonId, page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("content", activityPage.getContent());
        response.put("totalElements", activityPage.getTotalElements());
        response.put("totalPages", activityPage.getTotalPages());
        response.put("currentPage", activityPage.getNumber());
        response.put("size", activityPage.getSize());
        return ResponseEntity.ok(response);
    }
}
