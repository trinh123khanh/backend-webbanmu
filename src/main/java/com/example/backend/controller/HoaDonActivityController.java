package com.example.backend.controller;

import com.example.backend.dto.HoaDonActivityDTO;
import com.example.backend.service.HoaDonActivityService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý lịch sử thay đổi hóa đơn
 */
@RestController
@RequestMapping("/api/hoa-don-activity")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class HoaDonActivityController {

    private final HoaDonActivityService hoaDonActivityService;

    public HoaDonActivityController(HoaDonActivityService hoaDonActivityService) {
        this.hoaDonActivityService = hoaDonActivityService;
    }

    /**
     * Lấy danh sách lịch sử thay đổi hóa đơn với pagination
     * 
     * @param hoaDonId ID hóa đơn (optional) - nếu có thì chỉ lấy activities của hóa đơn đó
     * @param page Số trang (default: 0)
     * @param size Số lượng items mỗi trang (default: 20, max: 100)
     * @return Danh sách activities với pagination info
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Map<String, Object>> getActivities(
            @RequestParam(required = false) String hoaDonId,
            @RequestParam(required = false, defaultValue = "0") String page,
            @RequestParam(required = false, defaultValue = "20") String size) {
        try {
            // Parse hoaDonId từ String sang Long
            Long hoaDonIdLong = null;
            if (hoaDonId != null && !hoaDonId.trim().isEmpty()) {
                try {
                    hoaDonIdLong = Long.parseLong(hoaDonId.trim());
                    if (hoaDonIdLong <= 0) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "hoaDonId phải là số dương"));
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "hoaDonId không hợp lệ: " + hoaDonId));
                }
            }
            
            // Parse page và size từ String sang int
            int pageNumber = 0;
            try {
                pageNumber = Integer.parseInt(page != null ? page.trim() : "0");
                if (pageNumber < 0) {
                    pageNumber = 0;
                }
            } catch (NumberFormatException e) {
                pageNumber = 0;
            }
            
            int pageSize = 20;
            try {
                pageSize = Integer.parseInt(size != null ? size.trim() : "20");
                if (pageSize <= 0) {
                    pageSize = 20;
                }
                // Giới hạn pageSize tối đa
                if (pageSize > 100) {
                    pageSize = 100;
                }
            } catch (NumberFormatException e) {
                pageSize = 20;
            }
            
            Page<HoaDonActivityDTO> activityPage = hoaDonActivityService.getActivities(hoaDonIdLong, pageNumber, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", activityPage.getContent());
            response.put("totalElements", activityPage.getTotalElements());
            response.put("totalPages", activityPage.getTotalPages());
            response.put("currentPage", activityPage.getNumber());
            response.put("size", activityPage.getSize());
            response.put("first", activityPage.isFirst());
            response.put("last", activityPage.isLast());
            response.put("numberOfElements", activityPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getActivities: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy lịch sử thay đổi: " + e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    /**
     * Lấy lịch sử thay đổi của một hóa đơn cụ thể
     * 
     * @param hoaDonId ID hóa đơn
     * @param page Số trang (default: 0)
     * @param size Số lượng items mỗi trang (default: 20)
     * @return Danh sách activities của hóa đơn
     */
    @GetMapping("/hoa-don/{hoaDonId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Map<String, Object>> getActivitiesByHoaDonId(
            @PathVariable Long hoaDonId,
            @RequestParam(required = false, defaultValue = "0") String page,
            @RequestParam(required = false, defaultValue = "20") String size) {
        try {
            if (hoaDonId == null || hoaDonId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "hoaDonId phải là số dương"));
            }
            
            // Parse page và size
            int pageNumber = 0;
            try {
                pageNumber = Integer.parseInt(page != null ? page.trim() : "0");
                if (pageNumber < 0) {
                    pageNumber = 0;
                }
            } catch (NumberFormatException e) {
                pageNumber = 0;
            }
            
            int pageSize = 20;
            try {
                pageSize = Integer.parseInt(size != null ? size.trim() : "20");
                if (pageSize <= 0) {
                    pageSize = 20;
                }
                if (pageSize > 100) {
                    pageSize = 100;
                }
            } catch (NumberFormatException e) {
                pageSize = 20;
            }
            
            Page<HoaDonActivityDTO> activityPage = hoaDonActivityService.getActivities(hoaDonId, pageNumber, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", activityPage.getContent());
            response.put("totalElements", activityPage.getTotalElements());
            response.put("totalPages", activityPage.getTotalPages());
            response.put("currentPage", activityPage.getNumber());
            response.put("size", activityPage.getSize());
            response.put("first", activityPage.isFirst());
            response.put("last", activityPage.isLast());
            response.put("numberOfElements", activityPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getActivitiesByHoaDonId: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy lịch sử thay đổi: " + e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    /**
     * Lấy chi tiết một activity theo ID
     * 
     * @param id ID của activity
     * @return Chi tiết activity
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<HoaDonActivityDTO> getActivityById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            return hoaDonActivityService.getActivityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("❌ Error in getActivityById: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy danh sách activities với filter theo action
     * 
     * @param action Loại hành động (CREATE, UPDATE, DELETE, STATUS_CHANGE)
     * @param page Số trang (default: 0)
     * @param size Số lượng items mỗi trang (default: 20)
     * @return Danh sách activities
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Map<String, Object>> getActivitiesByAction(
            @PathVariable String action,
            @RequestParam(required = false, defaultValue = "0") String page,
            @RequestParam(required = false, defaultValue = "20") String size) {
        try {
            // Parse page và size
            int pageNumber = 0;
            try {
                pageNumber = Integer.parseInt(page != null ? page.trim() : "0");
                if (pageNumber < 0) {
                    pageNumber = 0;
                }
            } catch (NumberFormatException e) {
                pageNumber = 0;
            }
            
            int pageSize = 20;
            try {
                pageSize = Integer.parseInt(size != null ? size.trim() : "20");
                if (pageSize <= 0) {
                    pageSize = 20;
                }
                if (pageSize > 100) {
                    pageSize = 100;
                }
            } catch (NumberFormatException e) {
                pageSize = 20;
            }
            
            Page<HoaDonActivityDTO> activityPage = hoaDonActivityService.getActivitiesByAction(action, pageNumber, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", activityPage.getContent());
            response.put("totalElements", activityPage.getTotalElements());
            response.put("totalPages", activityPage.getTotalPages());
            response.put("currentPage", activityPage.getNumber());
            response.put("size", activityPage.getSize());
            response.put("first", activityPage.isFirst());
            response.put("last", activityPage.isLast());
            response.put("numberOfElements", activityPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getActivitiesByAction: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy lịch sử thay đổi: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}

