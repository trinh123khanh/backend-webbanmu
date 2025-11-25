package com.example.backend.controller;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.User;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.HoaDonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class CustomerOrdersController {

    private final HoaDonService hoaDonService;
    private final UserRepository userRepository;
    private final KhachHangRepository khachHangRepository;

    public CustomerOrdersController(
            HoaDonService hoaDonService,
            UserRepository userRepository,
            KhachHangRepository khachHangRepository) {
        this.hoaDonService = hoaDonService;
        this.userRepository = userRepository;
        this.khachHangRepository = khachHangRepository;
    }

    /**
     * Lấy danh sách đơn hàng đã thanh toán của khách hàng
     * QUAN TRỌNG: Cho phép xem lịch sử đơn hàng nếu đã đăng nhập (không nhất thiết phải có role CUSTOMER)
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrdersForCustomer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Lấy username từ authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Bạn cần đăng nhập để xem lịch sử đơn hàng");
                errorResponse.put("content", List.of());
                errorResponse.put("totalElements", 0);
                errorResponse.put("totalPages", 0);
                errorResponse.put("currentPage", page);
                errorResponse.put("size", size);
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            String username = auth.getName();
            System.out.println("📋 getOrdersForCustomer - Username: " + username);
            
            // Tìm User từ username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Tìm KhachHang từ userId - nếu chưa có, tìm theo email
            KhachHang khachHang = khachHangRepository.findByUserId(user.getId()).orElse(null);
            if (khachHang == null) {
                // Nếu chưa có KhachHang, tìm theo email
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    khachHang = khachHangRepository.findByEmail(user.getEmail()).orElse(null);
                }
                
                // Nếu vẫn chưa có, trả về danh sách rỗng (user chưa có đơn hàng nào)
                if (khachHang == null) {
                    System.out.println("⚠️ KhachHang not found for user: " + username + ", returning empty list");
                    Map<String, Object> response = new HashMap<>();
                    response.put("content", List.of());
                    response.put("totalElements", 0);
                    response.put("totalPages", 0);
                    response.put("currentPage", page);
                    response.put("size", size);
                    response.put("first", true);
                    response.put("last", true);
                    response.put("numberOfElements", 0);
                    return ResponseEntity.ok(response);
                }
            }
            
            // Gọi service để lấy đơn hàng đã thanh toán của khách hàng
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
            
            System.out.println("✅ getOrdersForCustomer - Found " + hoaDonDTOPage.getTotalElements() + " orders for customer " + khachHang.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getOrdersForCustomer: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy đơn hàng: " + e.getMessage());
            errorResponse.put("content", List.of());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("size", size);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Hủy đơn hàng của khách hàng
     */
    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrderForCustomer(@PathVariable Long id) {
        try {
            // Lấy username từ authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body("Bạn cần đăng nhập để hủy đơn hàng");
            }
            
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            KhachHang khachHang = khachHangRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("KhachHang not found for user: " + username));
            
            // Chỉ cho phép hủy đơn hàng ở trạng thái CHO_XAC_NHAN
            // Gọi service để cập nhật trạng thái
            HoaDonDTO updatedHoaDon = hoaDonService.updateTrangThaiHoaDon(id, "HUY");
            
            // Kiểm tra xem đơn hàng có thuộc về khách hàng này không
            if (!updatedHoaDon.getKhachHangId().equals(khachHang.getId())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body("Bạn không có quyền hủy đơn hàng này");
            }
            
            return ResponseEntity.ok(updatedHoaDon);
        } catch (Exception e) {
            System.err.println("❌ Error in cancelOrderForCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi hủy đơn hàng: " + e.getMessage());
        }
    }
}

