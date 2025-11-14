package com.example.backend.service;

import com.example.backend.dto.BestSellingProductDTO;
import com.example.backend.dto.PeriodStatisticsDTO;
import com.example.backend.dto.WeeklyRevenueDTO;
import com.example.backend.dto.OrderStatusStatisticsDTO;
import com.example.backend.dto.ChannelStatisticsDTO;
import com.example.backend.dto.BrandStatisticsDTO;
import com.example.backend.dto.LowStockProductDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonChiTiet;
import com.example.backend.entity.SanPham;
import com.example.backend.repository.HoaDonChiTietRepository;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.SanPhamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatisticsService {
    
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final SanPhamRepository sanPhamRepository;
    
    public StatisticsService(HoaDonChiTietRepository hoaDonChiTietRepository,
                           HoaDonRepository hoaDonRepository,
                           SanPhamRepository sanPhamRepository) {
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.sanPhamRepository = sanPhamRepository;
    }
    
    /**
     * Lấy top sản phẩm bán chạy dựa trên số lượng và đơn giá từ hóa đơn chi tiết
     * Logic:
     * - Từ hoa_don_chi_tiet lấy: so_luong, don_gia, chi_tiet_san_pham_id
     * - Từ chi_tiet_san_pham_id lấy: mau_sac_id, san_pham_id
     * - Từ mau_sac_id lấy: tên màu sắc
     * - Từ san_pham_id lấy: ten_san_pham, kieu_dang_mu_id
     * - Từ kieu_dang_mu_id lấy: tên kiểu dáng mũ
     * Sắp xếp theo độ bán chạy = tổng số lượng bán
     */
    public List<BestSellingProductDTO> getBestSellingProducts(int limit) {
        System.out.println("========================================");
        System.out.println("🔍 [StatisticsService] Starting getBestSellingProducts with limit: " + limit);
        System.out.println("========================================");
        
        // Kiểm tra tổng số bản ghi trong database
        try {
            long totalCountAll = hoaDonChiTietRepository.count();
            System.out.println("📊 [StatisticsService] Total invoice details in database (ALL): " + totalCountAll);
            
            long totalCountExcludingCancelled = hoaDonChiTietRepository.countAllExcludingCancelled();
            System.out.println("📊 [StatisticsService] Total invoice details (excluding cancelled): " + totalCountExcludingCancelled);
        } catch (Exception e) {
            System.err.println("⚠️ [StatisticsService] Could not count records: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Thử lấy tất cả hóa đơn chi tiết trước (không filter) để kiểm tra có dữ liệu không
        List<HoaDonChiTiet> chiTietList = new java.util.ArrayList<>();
        
        // Bước 1: Thử lấy tất cả không filter thời gian
        try {
            System.out.println("📋 [StatisticsService] Step 1: Trying to fetch all invoice details (no date filter, excluding cancelled)...");
            chiTietList = hoaDonChiTietRepository.findAllWithProductDetailsExcludingCancelled();
            System.out.println("✅ [StatisticsService] Step 1 SUCCESS: Found " + chiTietList.size() + " invoice detail records");
            
            // Nếu không có dữ liệu, thử query backup
            if (chiTietList.isEmpty()) {
                System.out.println("⚠️ [StatisticsService] Step 1 returned empty, trying backup query...");
                try {
                    chiTietList = hoaDonChiTietRepository.findAllWithProductDetailsExcludingCancelledBackup();
                    System.out.println("✅ [StatisticsService] Backup query SUCCESS: Found " + chiTietList.size() + " invoice detail records");
                } catch (Exception e3) {
                    System.err.println("⚠️ [StatisticsService] Backup query failed: " + e3.getMessage());
                    
                    // Thử lấy tất cả không filter gì cả (kể cả cancelled)
                    try {
                        List<HoaDonChiTiet> allRecords = hoaDonChiTietRepository.findAllWithAllDetails();
                        System.out.println("📊 [StatisticsService] Found " + allRecords.size() + " invoice detail records (ALL statuses)");
                        
                        if (!allRecords.isEmpty()) {
                            System.out.println("   ⚠️ All invoices might be cancelled, or query condition has issue");
                            System.out.println("   💡 Consider using allRecords if needed (commented out for now)");
                        }
                    } catch (Exception e4) {
                        System.err.println("⚠️ [StatisticsService] Could not fetch all records: " + e4.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [StatisticsService] Step 1 FAILED: Error in findAll query");
            System.err.println("   Error message: " + e.getMessage());
            System.err.println("   Error class: " + e.getClass().getName());
            e.printStackTrace();
            
            // Nếu query trên lỗi, thử query với date filter
            try {
                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = endDate.minusYears(1);
                System.out.println("🔄 [StatisticsService] Step 1 Fallback: Trying with date filter from " + startDate + " to " + endDate);
                chiTietList = hoaDonChiTietRepository.findWithProductDetailsByDateRange(startDate, endDate);
                System.out.println("✅ [StatisticsService] Step 1 Fallback SUCCESS: Found " + chiTietList.size() + " invoice detail records");
            } catch (Exception e2) {
                System.err.println("❌ [StatisticsService] Step 1 Fallback FAILED: Error in date filter query");
                System.err.println("   Error message: " + e2.getMessage());
                e2.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }
        
        if (chiTietList.isEmpty()) {
            System.out.println("⚠️ [StatisticsService] No invoice details found in database!");
            System.out.println("   Possible reasons:");
            System.out.println("   1. Database is empty - no data in hoa_don_chi_tiet table");
            System.out.println("   2. All invoices are cancelled (trangThai = 'DA_HUY')");
            System.out.println("   3. Query conditions are too restrictive");
            System.out.println("   4. JOIN FETCH might not be loading relationships properly");
            System.out.println("");
            System.out.println("   💡 Suggestion: Check database directly:");
            System.out.println("      SELECT COUNT(*) FROM hoa_don_chi_tiet;");
            System.out.println("      SELECT COUNT(*) FROM hoa_don WHERE trang_thai != 'DA_HUY';");
            System.out.println("========================================");
            return new java.util.ArrayList<>();
        }
        
        System.out.println("✅ [StatisticsService] Step 2: Processing " + chiTietList.size() + " invoice detail records...");
        
        return buildBestSellingProductsResponse(chiTietList, limit, "DEFAULT");
    }
    
    /**
     * Lấy sản phẩm bán chạy theo khoảng thời gian day/week/month/year giống bộ lọc thống kê
     */
    public List<BestSellingProductDTO> getBestSellingProductsByPeriod(String period, int limit) {
        System.out.println("========================================");
        System.out.println("🔍 [StatisticsService] Starting getBestSellingProductsByPeriod with period=" + period + ", limit=" + limit);
        System.out.println("========================================");
        
        DateRange dateRange = resolvePeriodDateRange(period);
        System.out.println("📅 [StatisticsService] Period date range: " + dateRange.getStart() + " -> " + dateRange.getEnd());
        
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findWithProductDetailsByDateRange(
            dateRange.getStart(),
            dateRange.getEnd()
        );
        System.out.println("📦 [StatisticsService] Found " + chiTietList.size() + " invoice detail records for period filter");
        
        return buildBestSellingProductsResponse(chiTietList, limit, "PERIOD:" + period);
    }
    
    /**
     * Lấy sản phẩm bán chạy theo khoảng ngày tùy chọn (custom date range)
     */
    public List<BestSellingProductDTO> getBestSellingProductsByDateRange(LocalDate startDate, LocalDate endDate, int limit) {
        System.out.println("========================================");
        System.out.println("🔍 [StatisticsService] Starting getBestSellingProductsByDateRange with startDate=" + startDate + ", endDate=" + endDate + ", limit=" + limit);
        System.out.println("========================================");
        
        if (startDate == null || endDate == null) {
            System.out.println("⚠️ [StatisticsService] Start date or end date is null, returning empty list.");
            return new ArrayList<>();
        }
        
        if (endDate.isBefore(startDate)) {
            System.out.println("⚠️ [StatisticsService] End date is before start date, returning empty list.");
            return new ArrayList<>();
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        System.out.println("📅 [StatisticsService] Custom date range (DateTime): " + startDateTime + " -> " + endDateTime);
        
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findWithProductDetailsByDateRange(startDateTime, endDateTime);
        System.out.println("📦 [StatisticsService] Found " + chiTietList.size() + " invoice detail records for custom date range");
        
        return buildBestSellingProductsResponse(chiTietList, limit, "CUSTOM_RANGE");
    }
    
    /**
     * Lấy thống kê theo khoảng thời gian (ngày, tuần, tháng, năm)
     * @param period Loại khoảng thời gian: "day", "week", "month", "year"
     * @return PeriodStatisticsDTO chứa doanh thu, số sản phẩm đã bán, số đơn hàng
     */
    public PeriodStatisticsDTO getPeriodStatistics(String period) {
        System.out.println("========================================");
        System.out.println("📊 [StatisticsService] Getting period statistics for: " + period);
        System.out.println("========================================");
        
        DateRange dateRange = resolvePeriodDateRange(period);
        LocalDateTime startDate = dateRange.getStart();
        LocalDateTime endDate = dateRange.getEnd();
        
        System.out.println("📅 [StatisticsService] Date range: " + startDate + " to " + endDate);
        
        // Debug: Kiểm tra tổng số hóa đơn trong DB
        long totalHoaDon = hoaDonRepository.count();
        System.out.println("📊 [StatisticsService] Total invoices in database: " + totalHoaDon);
        
        // Debug: Kiểm tra số hóa đơn không filter trạng thái
        List<HoaDon> allInPeriod = hoaDonRepository.findByNgayTaoBetween(startDate, endDate);
        System.out.println("📊 [StatisticsService] Invoices in period (all statuses): " + allInPeriod.size());
        if (!allInPeriod.isEmpty()) {
            System.out.println("   Sample invoice statuses:");
            for (int i = 0; i < Math.min(3, allInPeriod.size()); i++) {
                HoaDon hd = allInPeriod.get(i);
                System.out.println("   - Invoice #" + hd.getId() + ": status=" + hd.getTrangThai() + 
                                 ", ngayTao=" + hd.getNgayTao() + 
                                 ", thanhTien=" + hd.getThanhTien() +
                                 ", soLuongSP=" + hd.getSoLuongSanPham());
            }
        }
        
        // Lấy tất cả hóa đơn trong khoảng thời gian (trừ đơn đã hủy)
        List<HoaDon> hoaDonList = hoaDonRepository.findByNgayTaoBetweenExcludingCancelled(startDate, endDate);
        
        System.out.println("📦 [StatisticsService] Found " + hoaDonList.size() + " invoices in period (excluding cancelled)");
        
        // Tính toán thống kê
        BigDecimal doanhThu = BigDecimal.ZERO;
        BigDecimal actualRevenue = BigDecimal.ZERO; // Doanh thu thực tế (đã thanh toán)
        Integer sanPhamDaBan = 0;
        Integer donHang = hoaDonList.size();
        


        for (HoaDon hoaDon : hoaDonList) {
            // Tính tổng thanhTien
            if (hoaDon.getThanhTien() != null) {
                doanhThu = doanhThu.add(hoaDon.getThanhTien());
                System.out.println("   💰 Adding invoice #" + hoaDon.getId() + 
                                 " - thanhTien: " + hoaDon.getThanhTien() + 
                                 " (total now: " + doanhThu + ")");
            } else {
                System.out.println("   ⚠️ Invoice #" + hoaDon.getId() + " has null thanhTien");
            }
            
            // Kiểm tra xem hóa đơn đã thanh toán chưa (trạng thái DA_GIAO_HANG = Đã thanh toán)
            boolean isPaid = hoaDon.getTrangThai() == HoaDon.TrangThaiHoaDon.DA_GIAO_HANG;
            
            // Nếu đã thanh toán, cộng vào actualRevenue
            if (isPaid && hoaDon.getThanhTien() != null) {
                actualRevenue = actualRevenue.add(hoaDon.getThanhTien());
                System.out.println("   ✅ Invoice #" + hoaDon.getId() + " is paid (DA_GIAO_HANG), adding to actualRevenue");
            }
            
            // Tính tổng soLuongSanPham
            if (hoaDon.getSoLuongSanPham() != null) {
                sanPhamDaBan += hoaDon.getSoLuongSanPham();
                System.out.println("   📦 Adding invoice #" + hoaDon.getId() + 
                                 " - soLuongSanPham: " + hoaDon.getSoLuongSanPham() + 
                                 " (total now: " + sanPhamDaBan + ")");
            } else {
                System.out.println("   ⚠️ Invoice #" + hoaDon.getId() + " has null soLuongSanPham");
            }
        }
        
        // Tính công nợ = doanh thu - thực tế
        BigDecimal debtRevenue = doanhThu.subtract(actualRevenue);
        
        System.out.println("📊 [StatisticsService] Statistics calculated:");
        System.out.println("   - Doanh thu: " + doanhThu);
        System.out.println("   - Thực tế (đã thanh toán): " + actualRevenue);
        System.out.println("   - Công nợ: " + debtRevenue);
        System.out.println("   - Sản phẩm đã bán: " + sanPhamDaBan);
        System.out.println("   - Đơn hàng: " + donHang);
        System.out.println("========================================");
        
        return PeriodStatisticsDTO.builder()
                .doanhThu(doanhThu)
                .sanPhamDaBan(sanPhamDaBan)
                .donHang(donHang)
                .period(period)
                .actualRevenue(actualRevenue)
                .debtRevenue(debtRevenue)
                .build();
    }
    
    /**
     * Lấy thống kê theo khoảng thời gian tùy chỉnh (từ ngày đến ngày)
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return PeriodStatisticsDTO chứa doanh thu, số sản phẩm đã bán, số đơn hàng
     */
    public PeriodStatisticsDTO getPeriodStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        System.out.println("========================================");
        System.out.println("📊 [StatisticsService] Getting statistics by date range: " + startDate + " to " + endDate);
        System.out.println("========================================");
        
        // Chuyển đổi LocalDate sang LocalDateTime (bắt đầu từ 00:00:00 và kết thúc ở 23:59:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        System.out.println("📅 [StatisticsService] DateTime range: " + startDateTime + " to " + endDateTime);
        
        // Lấy tất cả hóa đơn trong khoảng thời gian (trừ đơn đã hủy)
        List<HoaDon> hoaDonList = hoaDonRepository.findByNgayTaoBetweenExcludingCancelled(startDateTime, endDateTime);
        
        System.out.println("📦 [StatisticsService] Found " + hoaDonList.size() + " invoices in date range (excluding cancelled)");
        
        // Tính toán thống kê
        BigDecimal doanhThu = BigDecimal.ZERO;
        BigDecimal actualRevenue = BigDecimal.ZERO; // Doanh thu thực tế (đã thanh toán)
        Integer sanPhamDaBan = 0;
        Integer donHang = hoaDonList.size();
        
        for (HoaDon hoaDon : hoaDonList) {
            // Tính tổng thanhTien
            if (hoaDon.getThanhTien() != null) {
                doanhThu = doanhThu.add(hoaDon.getThanhTien());
            }
            
            // Kiểm tra xem hóa đơn đã thanh toán chưa (trạng thái DA_GIAO_HANG = Đã thanh toán)
            boolean isPaid = hoaDon.getTrangThai() == HoaDon.TrangThaiHoaDon.DA_GIAO_HANG;
            
            // Nếu đã thanh toán, cộng vào actualRevenue
            if (isPaid && hoaDon.getThanhTien() != null) {
                actualRevenue = actualRevenue.add(hoaDon.getThanhTien());
            }
            
            // Tính tổng soLuongSanPham
            if (hoaDon.getSoLuongSanPham() != null) {
                sanPhamDaBan += hoaDon.getSoLuongSanPham();
            }
        }
        
        // Tính công nợ = doanh thu - thực tế
        BigDecimal debtRevenue = doanhThu.subtract(actualRevenue);
        
        System.out.println("📊 [StatisticsService] Statistics calculated:");
        System.out.println("   - Doanh thu: " + doanhThu);
        System.out.println("   - Thực tế (đã thanh toán): " + actualRevenue);
        System.out.println("   - Công nợ: " + debtRevenue);
        System.out.println("   - Sản phẩm đã bán: " + sanPhamDaBan);
        System.out.println("   - Đơn hàng: " + donHang);
        System.out.println("========================================");
        
        return PeriodStatisticsDTO.builder()
                .doanhThu(doanhThu)
                .sanPhamDaBan(sanPhamDaBan)
                .donHang(donHang)
                .period("custom") // Đánh dấu là custom date range
                .actualRevenue(actualRevenue)
                .debtRevenue(debtRevenue)
                .build();
    }

    private List<BestSellingProductDTO> buildBestSellingProductsResponse(List<HoaDonChiTiet> chiTietList, int limit, String contextLabel) {
        if (chiTietList == null || chiTietList.isEmpty()) {
            System.out.println("⚠️ [StatisticsService] No invoice details found for context=" + contextLabel);
            return new ArrayList<>();
        }
        
        Map<Long, BestSellingProductDTO> productMap = new HashMap<>();
        int skippedCount = 0;
        int processedCount = 0;
        
        System.out.println("📦 [StatisticsService] Processing " + chiTietList.size() + " invoice detail records for context=" + contextLabel);
        
        for (HoaDonChiTiet hdct : chiTietList) {
            processedCount++;
            
            if (hdct == null || hdct.getChiTietSanPham() == null || hdct.getChiTietSanPham().getSanPham() == null) {
                System.out.println("⚠️ [StatisticsService] Record #" + processedCount + " skipped due to missing references (context=" + contextLabel + ")");
                skippedCount++;
                continue;
            }
            
            Long chiTietSanPhamId = hdct.getChiTietSanPham().getId();
            
            if (!productMap.containsKey(chiTietSanPhamId)) {
                var chiTietSP = hdct.getChiTietSanPham();
                var sanPham = chiTietSP.getSanPham();
                
                String mauSac = chiTietSP.getMauSac() != null ? chiTietSP.getMauSac().getTenMau() : null;
                String tenSanPham = sanPham.getTenSanPham();
                String kieuDang = sanPham.getKieuDangMu() != null ? sanPham.getKieuDangMu().getTenKieuDang() : null;
                
                BestSellingProductDTO dto = BestSellingProductDTO.builder()
                        .chiTietSanPhamId(chiTietSanPhamId)
                        .sanPhamId(sanPham.getId())
                        .tenSanPham(tenSanPham)
                        .mauSac(mauSac)
                        .kieuDang(kieuDang)
                        .donGia(hdct.getDonGia())
                        .soLuongBan(0)
                        .build();
                
                productMap.put(chiTietSanPhamId, dto);
            }
            
            BestSellingProductDTO dto = productMap.get(chiTietSanPhamId);
            dto.setSoLuongBan(dto.getSoLuongBan() + hdct.getSoLuong());
        }
        
        System.out.println("📈 [StatisticsService] Processing summary (" + contextLabel + "):");
        System.out.println("   - Total records processed: " + chiTietList.size());
        System.out.println("   - Records skipped: " + skippedCount);
        System.out.println("   - Product groups created: " + productMap.size());
        
        if (productMap.isEmpty()) {
            System.out.println("⚠️ [StatisticsService] No valid products after processing for context=" + contextLabel);
            return new ArrayList<>();
        }
        
        List<BestSellingProductDTO> result = productMap.values().stream()
            .sorted((a, b) -> Integer.compare(b.getSoLuongBan(), a.getSoLuongBan()))
            .limit(limit)
            .collect(Collectors.toList());
        
        System.out.println("✅ [StatisticsService] Returning " + result.size() + " best selling products for context=" + contextLabel);
        return result;
    }

    private DateRange resolvePeriodDateRange(String period) {
        LocalDate today = LocalDate.now();
        LocalDateTime startDate;
        LocalDateTime endDate;
        
        switch (period == null ? "month" : period.toLowerCase()) {
            case "day":
            case "today":
                startDate = today.atStartOfDay();
                endDate = LocalDateTime.now();
                break;
            case "week":
                startDate = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
                endDate = LocalDateTime.now();
                break;
            case "year":
                startDate = LocalDate.of(2025, 1, 1).atStartOfDay();
                endDate = LocalDate.of(2026, 1, 1).atStartOfDay();
                break;
            case "month":
            default:
                if (period != null && !List.of("day", "today", "week", "month", "year").contains(period.toLowerCase())) {
                    System.err.println("⚠️ [StatisticsService] Invalid period: " + period + ", defaulting to month");
                }
                startDate = LocalDate.of(2025, 11, 1).atStartOfDay();
                endDate = LocalDate.of(2025, 12, 1).atStartOfDay();
                break;
        }
        
        return new DateRange(startDate, endDate);
    }

    private static class DateRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }
    }
    
    /**
     * Lấy tổng số hóa đơn trong database (tất cả)
     */
    public long getTotalInvoiceCount() {
        return hoaDonRepository.count();
    }
    
    /**
     * Lấy tổng số hóa đơn không bị hủy
     */
    public long getTotalInvoiceCountExcludingCancelled() {
        return hoaDonRepository.findAll().stream()
                .filter(h -> h.getTrangThai() != HoaDon.TrangThaiHoaDon.DA_HUY)
                .count();
    }
    
    /**
     * Lấy thống kê doanh thu theo tuần trong tháng (tháng 11/2025: 1/11/2025 - 1/12/2025)
     * @return Danh sách WeeklyRevenueDTO chứa doanh thu theo từng tuần
     */
    public List<WeeklyRevenueDTO> getWeeklyRevenueForMonth() {
        System.out.println("========================================");
        System.out.println("📈 [StatisticsService] Getting weekly revenue for month 11/2025");
        System.out.println("========================================");
        
        // Tháng 11/2025: từ 1/11/2025 đến 1/12/2025
        LocalDate monthStart = LocalDate.of(2025, 11, 1);
        LocalDate monthEnd = LocalDate.of(2025, 12, 1);
        
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = monthEnd.atStartOfDay();
        
        System.out.println("📅 [StatisticsService] Month range: " + startDateTime + " to " + endDateTime);
        
        // Lấy tất cả hóa đơn trong tháng (trừ đơn đã hủy)
        List<HoaDon> hoaDonList = hoaDonRepository.findByNgayTaoBetweenExcludingCancelled(startDateTime, endDateTime);
        
        System.out.println("📦 [StatisticsService] Found " + hoaDonList.size() + " invoices in month 11/2025");
        
        // Chia tháng thành các tuần
        List<WeeklyRevenueDTO> weeklyRevenues = new ArrayList<>();
        LocalDate currentDate = monthStart;
        int weekNumber = 1;
        
        while (currentDate.isBefore(monthEnd)) {
            // Xác định ngày bắt đầu tuần (Thứ 2)
            LocalDate weekStart = currentDate;
            if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
                // Nếu không phải Thứ 2, tìm Thứ 2 gần nhất trước đó (hoặc giữ nguyên nếu là ngày đầu tháng)
                if (weekStart.getDayOfWeek().getValue() > DayOfWeek.MONDAY.getValue()) {
                    weekStart = weekStart.minusDays(weekStart.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
                }
                // Nếu tuần bắt đầu trước tháng, đặt về ngày đầu tháng
                if (weekStart.isBefore(monthStart)) {
                    weekStart = monthStart;
                }
            }
            
            // Xác định ngày kết thúc tuần (Chủ nhật hoặc cuối tháng)
            LocalDate weekEnd = weekStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            if (weekEnd.isAfter(monthEnd) || weekEnd.isEqual(monthEnd)) {
                weekEnd = monthEnd.minusDays(1); // Trừ 1 vì monthEnd là 1/12 (không tính)
            }
            
            // Nếu tuần không hợp lệ (weekStart > weekEnd), bỏ qua
            if (weekStart.isAfter(weekEnd)) {
                break;
            }
            
            System.out.println("📅 [StatisticsService] Week " + weekNumber + ": " + weekStart + " to " + weekEnd);
            
            // Tính tổng doanh thu và số đơn hàng trong tuần này
            BigDecimal weekRevenue = BigDecimal.ZERO;
            int weekOrders = 0;
            
            for (HoaDon hoaDon : hoaDonList) {
                LocalDate invoiceDate = hoaDon.getNgayTao().toLocalDate();
                
                // Kiểm tra xem hóa đơn có thuộc tuần này không
                if (!invoiceDate.isBefore(weekStart) && !invoiceDate.isAfter(weekEnd)) {
                    if (hoaDon.getThanhTien() != null) {
                        weekRevenue = weekRevenue.add(hoaDon.getThanhTien());
                    }
                    weekOrders++;
                }
            }
            
            System.out.println("   💰 Week " + weekNumber + " revenue: " + weekRevenue + ", orders: " + weekOrders);
            
            weeklyRevenues.add(WeeklyRevenueDTO.builder()
                    .weekLabel("Tuần " + weekNumber)
                    .startDate(weekStart)
                    .endDate(weekEnd)
                    .totalRevenue(weekRevenue)
                    .totalOrders(weekOrders)
                    .build());
            
            // Chuyển sang tuần tiếp theo (bắt đầu từ ngày sau Chủ nhật)
            currentDate = weekEnd.plusDays(1);
            weekNumber++;
            
            // Nếu đã vượt quá cuối tháng, dừng lại
            if (currentDate.isAfter(monthEnd) || currentDate.isEqual(monthEnd)) {
                break;
            }
        }
        
        System.out.println("✅ [StatisticsService] Returning " + weeklyRevenues.size() + " weeks of revenue data");
        System.out.println("========================================");
        
        return weeklyRevenues;
    }

    /**
     * Lấy thống kê trạng thái đơn hàng theo khoảng thời gian
     * @param period Loại khoảng thời gian: "day", "week", "month", "year"
     * @return Danh sách OrderStatusStatisticsDTO chứa thống kê theo trạng thái
     */
    public List<OrderStatusStatisticsDTO> getOrderStatusStatistics(String period) {
        System.out.println("========================================");
        System.out.println("📊 [StatisticsService] Getting order status statistics for: " + period);
        System.out.println("========================================");
        
        // Xác định khoảng thời gian
        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = getEndDateForPeriod(period);
        
        System.out.println("📅 [StatisticsService] Date range: " + startDate + " to " + endDate);
        
        // Lấy tất cả hóa đơn trong khoảng thời gian (kể cả đã hủy để hiển thị đầy đủ)
        List<HoaDon> hoaDonList = hoaDonRepository.findByNgayTaoBetween(startDate, endDate);
        
        System.out.println("📦 [StatisticsService] Found " + hoaDonList.size() + " invoices in period");
        
        // Map màu sắc cho các trạng thái
        Map<HoaDon.TrangThaiHoaDon, String> colorMap = new HashMap<>();
        colorMap.put(HoaDon.TrangThaiHoaDon.CHO_XAC_NHAN, "#f472b6");  // Pink
        colorMap.put(HoaDon.TrangThaiHoaDon.DA_XAC_NHAN, "#fbbf24");   // Yellow
        colorMap.put(HoaDon.TrangThaiHoaDon.DANG_GIAO_HANG, "#14b8a6"); // Green
        colorMap.put(HoaDon.TrangThaiHoaDon.DA_GIAO_HANG, "#a855f7");  // Purple
        colorMap.put(HoaDon.TrangThaiHoaDon.DA_HUY, "#ef4444");        // Red
        
        // Map tên hiển thị cho các trạng thái
        Map<HoaDon.TrangThaiHoaDon, String> labelMap = new HashMap<>();
        labelMap.put(HoaDon.TrangThaiHoaDon.CHO_XAC_NHAN, "Chờ xác nhận");
        labelMap.put(HoaDon.TrangThaiHoaDon.DA_XAC_NHAN, "Chờ giao hàng");
        labelMap.put(HoaDon.TrangThaiHoaDon.DANG_GIAO_HANG, "Đang giao");
        labelMap.put(HoaDon.TrangThaiHoaDon.DA_GIAO_HANG, "Hoàn thành");
        labelMap.put(HoaDon.TrangThaiHoaDon.DA_HUY, "Đã hủy");
        
        // Đếm số lượng theo từng trạng thái
        Map<HoaDon.TrangThaiHoaDon, Integer> statusCountMap = new HashMap<>();
        
        for (HoaDon hoaDon : hoaDonList) {
            HoaDon.TrangThaiHoaDon status = hoaDon.getTrangThai();
            statusCountMap.put(status, statusCountMap.getOrDefault(status, 0) + 1);
        }
        
        // Tạo danh sách DTO kết quả - LUÔN hiển thị tất cả trạng thái, kể cả khi count = 0
        List<OrderStatusStatisticsDTO> result = new ArrayList<>();
        
        // Thứ tự hiển thị theo frontend
        HoaDon.TrangThaiHoaDon[] displayOrder = {
            HoaDon.TrangThaiHoaDon.CHO_XAC_NHAN,
            HoaDon.TrangThaiHoaDon.DA_XAC_NHAN,
            HoaDon.TrangThaiHoaDon.DANG_GIAO_HANG,
            HoaDon.TrangThaiHoaDon.DA_GIAO_HANG,
            HoaDon.TrangThaiHoaDon.DA_HUY
        };
        
        for (HoaDon.TrangThaiHoaDon status : displayOrder) {
            int count = statusCountMap.getOrDefault(status, 0);
            result.add(OrderStatusStatisticsDTO.builder()
                    .label(labelMap.get(status))
                    .count(count)
                    .color(colorMap.get(status))
                    .statusCode(status.name())
                    .build());
            
            System.out.println("   📊 " + labelMap.get(status) + ": " + count);
        }
        
        System.out.println("✅ [StatisticsService] Order status statistics calculated");
        System.out.println("========================================");
        
        return result;
    }

    /**
     * Lấy thống kê kênh bán hàng (Online vs Tại quầy)
     * Logic: Nếu nhanVienId != null thì là "Tại quầy", null thì là "Online"
     * @return Danh sách ChannelStatisticsDTO
     */
    public List<ChannelStatisticsDTO> getChannelStatistics() {
        System.out.println("========================================");
        System.out.println("📊 [StatisticsService] Getting channel statistics");
        System.out.println("========================================");
        
        // Lấy tất cả hóa đơn (trừ đơn đã hủy)
        List<HoaDon> hoaDonList = hoaDonRepository.findAll().stream()
                .filter(h -> h.getTrangThai() != HoaDon.TrangThaiHoaDon.DA_HUY)
                .collect(Collectors.toList());
        
        System.out.println("📦 [StatisticsService] Found " + hoaDonList.size() + " invoices (excluding cancelled)");
        
        int onlineCount = 0;
        int inStoreCount = 0;
        
        // Phân loại theo nhanVienId
        for (HoaDon hoaDon : hoaDonList) {
            if (hoaDon.getNhanVien() != null) {
                inStoreCount++;
            } else {
                onlineCount++;
            }
        }
        
        System.out.println("   📊 Online: " + onlineCount);
        System.out.println("   📊 Tại quầy: " + inStoreCount);
        
        // Tạo danh sách kết quả - LUÔN hiển thị cả 2 kênh, kể cả khi count = 0
        List<ChannelStatisticsDTO> result = new ArrayList<>();
        result.add(ChannelStatisticsDTO.builder()
                .channel("Online")
                .count(onlineCount)
                .color("#f472b6")
                .build());
        result.add(ChannelStatisticsDTO.builder()
                .channel("Tại quầy")
                .count(inStoreCount)
                .color("#3b82f6")
                .build());
        
        System.out.println("✅ [StatisticsService] Channel statistics calculated");
        System.out.println("========================================");
        
        return result;
    }

    /**
     * Helper method: Lấy start date cho period
     */
    private LocalDateTime getStartDateForPeriod(String period) {
        LocalDate today = LocalDate.now();
        switch (period.toLowerCase()) {
            case "day":
            case "today":
                return today.atStartOfDay();
            case "week":
                return today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
            case "month":
                return LocalDate.of(2025, 11, 1).atStartOfDay();
            case "year":
                return LocalDate.of(2025, 1, 1).atStartOfDay();
            default:
                return LocalDate.of(2025, 11, 1).atStartOfDay();
        }
    }

    /**
     * Helper method: Lấy end date cho period
     */
    private LocalDateTime getEndDateForPeriod(String period) {
        switch (period.toLowerCase()) {
            case "day":
            case "today":
                return LocalDateTime.now();
            case "week":
                return LocalDateTime.now();
            case "month":
                return LocalDate.of(2025, 12, 1).atStartOfDay();
            case "year":
                return LocalDate.of(2026, 1, 1).atStartOfDay();
            default:
                return LocalDate.of(2025, 12, 1).atStartOfDay();
        }
    }

    /**
     * Lấy thống kê top hãng bán chạy dựa trên số lượng sản phẩm đã bán
     * @param limit Số lượng hãng top cần lấy
     * @return Danh sách BrandStatisticsDTO
     */
    public List<BrandStatisticsDTO> getTopBrands(int limit) {
        System.out.println("========================================");
        System.out.println("📊 [StatisticsService] Getting top brands with limit: " + limit);
        System.out.println("========================================");
        
        // Lấy tất cả hóa đơn chi tiết (trừ đơn đã hủy) - đã có JOIN FETCH nhaSanXuat
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findAllWithProductDetailsExcludingCancelled();
        
        System.out.println("📦 [StatisticsService] Found " + chiTietList.size() + " invoice details (excluding cancelled)");
        
        // Nhóm theo nhà sản xuất và tính tổng số lượng
        Map<Long, BrandStatisticsDTO> brandMap = new HashMap<>();
        int skippedCount = 0;
        
        for (HoaDonChiTiet hdct : chiTietList) {
            // Kiểm tra null
            if (hdct == null || hdct.getChiTietSanPham() == null 
                || hdct.getChiTietSanPham().getSanPham() == null) {
                skippedCount++;
                continue;
            }
            
            var sanPham = hdct.getChiTietSanPham().getSanPham();
            
            // Lazy load nhaSanXuat
            var nhaSanXuat = sanPham.getNhaSanXuat();
            if (nhaSanXuat == null) {
                skippedCount++;
                continue;
            }
            
            Long nhaSanXuatId = nhaSanXuat.getId();
            
            if (!brandMap.containsKey(nhaSanXuatId)) {
                // Tạo mới DTO nếu chưa có
                BrandStatisticsDTO dto = BrandStatisticsDTO.builder()
                    .nhaSanXuatId(nhaSanXuatId)
                    .tenNhaSanXuat(nhaSanXuat.getTenNhaSanXuat())
                    .tongSoLuongMua(0) // Sẽ được cộng dồn sau
                    .build();
                
                brandMap.put(nhaSanXuatId, dto);
            }
            
            // Cộng dồn số lượng từ hoa_don_chi_tiet.so_luong
            BrandStatisticsDTO dto = brandMap.get(nhaSanXuatId);
            dto.setTongSoLuongMua(dto.getTongSoLuongMua() + hdct.getSoLuong());
        }
        
        System.out.println("📊 [StatisticsService] Found " + brandMap.size() + " unique brands (skipped " + skippedCount + " records)");
        
        // Sắp xếp theo số lượng mua giảm dần và lấy top
        List<BrandStatisticsDTO> result = brandMap.values().stream()
            .sorted((a, b) -> Integer.compare(b.getTongSoLuongMua(), a.getTongSoLuongMua()))
            .limit(limit)
            .collect(Collectors.toList());
        
        System.out.println("✅ [StatisticsService] Returning " + result.size() + " top brands:");
        for (int i = 0; i < result.size(); i++) {
            BrandStatisticsDTO dto = result.get(i);
            System.out.println("   " + (i + 1) + ". " + dto.getTenNhaSanXuat() + " | SL: " + dto.getTongSoLuongMua());
        }
        
        return result;
    }

    /**
     * Lấy danh sách sản phẩm sắp hết hàng (số lượng <= threshold)
     * @param threshold Ngưỡng số lượng (ví dụ: 5)
     * @param limit Số lượng sản phẩm cần lấy
     * @return Danh sách LowStockProductDTO
     */
    public List<LowStockProductDTO> getLowStockProducts(int threshold, int limit) {
        System.out.println("========================================");
        System.out.println("📊 [StatisticsService] Getting low stock products with threshold: " + threshold + ", limit: " + limit);
        System.out.println("========================================");
        
        // Lấy tất cả sản phẩm từ database
        List<SanPham> allProducts = sanPhamRepository.findAll();
        
        System.out.println("📦 [StatisticsService] Found " + allProducts.size() + " total products");
        
        // Lọc sản phẩm có số lượng <= threshold và sort theo số lượng
        List<LowStockProductDTO> lowStockProducts = allProducts.stream()
            .filter(sp -> sp != null 
                && sp.getSoLuongTon() != null 
                && sp.getSoLuongTon() <= threshold
                && sp.getTrangThai() != null 
                && sp.getTrangThai()) // Chỉ lấy sản phẩm đang hoạt động
            .sorted(Comparator.comparing(SanPham::getSoLuongTon))
            .limit(limit)
            .map(sp -> LowStockProductDTO.builder()
                .sanPhamId(sp.getId())
                .tenSanPham(sp.getTenSanPham())
                .soLuongTon(sp.getSoLuongTon())
                .build())
            .collect(Collectors.toList());
        
        System.out.println("✅ [StatisticsService] Returning " + lowStockProducts.size() + " low stock products:");
        for (int i = 0; i < lowStockProducts.size(); i++) {
            LowStockProductDTO dto = lowStockProducts.get(i);
            System.out.println("   " + (i + 1) + ". " + dto.getTenSanPham() + " | SL: " + dto.getSoLuongTon());
        }
        System.out.println("========================================");
        
        return lowStockProducts;
    }
}

