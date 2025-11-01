package com.example.backend.controller;

import com.example.backend.dto.BestSellingProductDTO;
import com.example.backend.dto.PeriodStatisticsDTO;
import com.example.backend.dto.WeeklyRevenueDTO;
import com.example.backend.entity.HoaDonChiTiet;
import com.example.backend.repository.HoaDonChiTietRepository;
import com.example.backend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    
    public StatisticsController(StatisticsService statisticsService, 
                               HoaDonChiTietRepository hoaDonChiTietRepository) {
        this.statisticsService = statisticsService;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
    }
    
    @GetMapping("/best-selling-products")
    public ResponseEntity<?> getBestSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            System.out.println("========================================");
            System.out.println("📥 [StatisticsController] Received GET request: /api/statistics/best-selling-products?limit=" + limit);
            System.out.println("========================================");
            
            List<BestSellingProductDTO> products = statisticsService.getBestSellingProducts(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", products);
            response.put("total", products.size());
            
            System.out.println("✅ [StatisticsController] Successfully returning " + products.size() + " products");
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ [StatisticsController] ERROR occurred:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            System.err.println("========================================");
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy danh sách sản phẩm bán chạy: " + e.getMessage());
            errorResponse.put("data", List.of());
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Endpoint test đơn giản để kiểm tra controller có hoạt động không
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "StatisticsController is working!",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    // Endpoint test để kiểm tra period endpoint có được register không
    @GetMapping("/period-test")
    public ResponseEntity<?> periodTest() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "Period endpoint is accessible",
            "endpoints", List.of(
                "/api/statistics/period?period=day",
                "/api/statistics/period?period=week",
                "/api/statistics/period?period=month",
                "/api/statistics/period?period=year"
            )
        ));
    }
    
    // Endpoint test để kiểm tra có dữ liệu không
    @GetMapping("/test-data")
    public ResponseEntity<?> testData() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Đếm tổng số hóa đơn chi tiết
            long totalCount = hoaDonChiTietRepository.countAllExcludingCancelled();
            result.put("totalInvoiceDetails", totalCount);
            
            // Lấy 5 bản ghi đầu tiên để kiểm tra
            List<HoaDonChiTiet> sample = hoaDonChiTietRepository.findAllWithProductDetailsExcludingCancelled();
            result.put("sampleSize", sample.size());
            
            if (!sample.isEmpty()) {
                List<Map<String, Object>> sampleData = new java.util.ArrayList<>();
                for (int i = 0; i < Math.min(5, sample.size()); i++) {
                    HoaDonChiTiet hdct = sample.get(i);
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", hdct.getId());
                    item.put("soLuong", hdct.getSoLuong());
                    item.put("donGia", hdct.getDonGia());
                    
                    if (hdct.getChiTietSanPham() != null) {
                        item.put("chiTietSanPhamId", hdct.getChiTietSanPham().getId());
                        if (hdct.getChiTietSanPham().getSanPham() != null) {
                            item.put("tenSanPham", hdct.getChiTietSanPham().getSanPham().getTenSanPham());
                        }
                        if (hdct.getChiTietSanPham().getMauSac() != null) {
                            item.put("mauSac", hdct.getChiTietSanPham().getMauSac().getTenMau());
                        }
                    }
                    if (hdct.getHoaDon() != null) {
                        item.put("hoaDonId", hdct.getHoaDon().getId());
                        item.put("trangThai", hdct.getHoaDon().getTrangThai());
                    }
                    sampleData.add(item);
                }
                result.put("sampleData", sampleData);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("stackTrace", e.getClass().getName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Lấy thống kê theo khoảng thời gian (ngày, tuần, tháng, năm)
     * @param period Loại khoảng thời gian: "day", "week", "month", "year"
     * @return PeriodStatisticsDTO chứa doanh thu, số sản phẩm đã bán, số đơn hàng
     */
    @GetMapping("/period")
    public ResponseEntity<?> getPeriodStatistics(
            @RequestParam(defaultValue = "month") String period) {
        try {
            System.out.println("========================================");
            System.out.println("📥 [StatisticsController] Received GET request: /api/statistics/period?period=" + period);
            System.out.println("========================================");
            
            PeriodStatisticsDTO statistics = statisticsService.getPeriodStatistics(period);
            
            System.out.println("✅ [StatisticsController] Successfully returning period statistics");
            System.out.println("   - Period: " + statistics.getPeriod());
            System.out.println("   - Doanh thu: " + statistics.getDoanhThu());
            System.out.println("   - Sản phẩm đã bán: " + statistics.getSanPhamDaBan());
            System.out.println("   - Đơn hàng: " + statistics.getDonHang());
            System.out.println("========================================");
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ [StatisticsController] ERROR occurred:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            System.err.println("========================================");
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy thống kê theo khoảng thời gian: " + e.getMessage());
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint debug để kiểm tra dữ liệu trong database
     */
    @GetMapping("/period-debug")
    public ResponseEntity<?> getPeriodStatisticsDebug(
            @RequestParam(defaultValue = "month") String period) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Tổng số hóa đơn trong DB
            long totalHoaDon = statisticsService.getTotalInvoiceCount();
            debugInfo.put("totalInvoicesInDB", totalHoaDon);
            
            // Tổng số hóa đơn không hủy
            long totalNotCancelled = statisticsService.getTotalInvoiceCountExcludingCancelled();
            debugInfo.put("totalInvoicesNotCancelled", totalNotCancelled);
            
            // Lấy thống kê
            PeriodStatisticsDTO statistics = statisticsService.getPeriodStatistics(period);
            debugInfo.put("statistics", statistics);
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("stackTrace", e.getClass().getName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Lấy thống kê doanh thu theo tuần trong tháng (tháng 11/2025) cho biểu đồ
     * @return Danh sách WeeklyRevenueDTO chứa doanh thu theo từng tuần
     */
    @GetMapping("/weekly-revenue")
    public ResponseEntity<?> getWeeklyRevenue() {
        try {
            System.out.println("========================================");
            System.out.println("📥 [StatisticsController] Received GET request: /api/statistics/weekly-revenue");
            System.out.println("========================================");
            
            List<WeeklyRevenueDTO> weeklyRevenue = statisticsService.getWeeklyRevenueForMonth();
            
            System.out.println("✅ [StatisticsController] Successfully returning " + weeklyRevenue.size() + " weeks of revenue data");
            System.out.println("========================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", weeklyRevenue);
            response.put("total", weeklyRevenue.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ [StatisticsController] ERROR occurred:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            System.err.println("========================================");
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy thống kê doanh thu theo tuần: " + e.getMessage());
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", List.of());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

