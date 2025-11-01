package com.example.backend.service;

import com.example.backend.dto.BestSellingProductDTO;
import com.example.backend.dto.PeriodStatisticsDTO;
import com.example.backend.dto.WeeklyRevenueDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonChiTiet;
import com.example.backend.repository.HoaDonChiTietRepository;
import com.example.backend.repository.HoaDonRepository;
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
    
    public StatisticsService(HoaDonChiTietRepository hoaDonChiTietRepository,
                           HoaDonRepository hoaDonRepository) {
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.hoaDonRepository = hoaDonRepository;
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
        
        // Nhóm theo chi_tiet_san_pham_id và tính tổng số lượng
        Map<Long, BestSellingProductDTO> productMap = new HashMap<>();
        int skippedCount = 0;
        int processedCount = 0;
        
        System.out.println("📦 [StatisticsService] Step 3: Grouping products by chiTietSanPhamId...");
        
        for (HoaDonChiTiet hdct : chiTietList) {
            processedCount++;
            
            // Kiểm tra null
            if (hdct == null) {
                System.out.println("⚠️ [StatisticsService] Record #" + processedCount + ": hdct is null");
                skippedCount++;
                continue;
            }
            
            if (hdct.getChiTietSanPham() == null) {
                System.out.println("⚠️ [StatisticsService] Record #" + processedCount + " (id=" + hdct.getId() + "): chiTietSanPham is null");
                skippedCount++;
                continue;
            }
            
            if (hdct.getChiTietSanPham().getSanPham() == null) {
                Long chiTietSanPhamId = hdct.getChiTietSanPham().getId();
                System.out.println("⚠️ [StatisticsService] Record #" + processedCount + " (chiTietSanPhamId=" + chiTietSanPhamId + "): sanPham is null");
                skippedCount++;
                continue;
            }
            
            Long chiTietSanPhamId = hdct.getChiTietSanPham().getId();
            
            if (!productMap.containsKey(chiTietSanPhamId)) {
                // Tạo mới DTO nếu chưa có
                var chiTietSP = hdct.getChiTietSanPham();
                var sanPham = chiTietSP.getSanPham();
                
                // Lấy màu sắc từ chi_tiet_san_pham -> mau_sac_id -> mau_sac.ten_mau
                String mauSac = null;
                if (chiTietSP.getMauSac() != null) {
                    mauSac = chiTietSP.getMauSac().getTenMau();
                }
                
                // Lấy tên sản phẩm từ san_pham.ten_san_pham
                String tenSanPham = sanPham.getTenSanPham();
                
                // Lấy kiểu dáng mũ từ san_pham -> kieu_dang_mu_id -> kieu_dang_mu.ten_kieu_dang
                String kieuDang = null;
                if (sanPham.getKieuDangMu() != null) {
                    kieuDang = sanPham.getKieuDangMu().getTenKieuDang();
                }
                
                BestSellingProductDTO dto = BestSellingProductDTO.builder()
                    .chiTietSanPhamId(chiTietSanPhamId)
                    .sanPhamId(sanPham.getId())
                    .tenSanPham(tenSanPham)
                    .mauSac(mauSac)
                    .kieuDang(kieuDang)
                    .donGia(hdct.getDonGia()) // Lấy từ hoa_don_chi_tiet.don_gia
                    .soLuongBan(0) // Sẽ được cộng dồn sau
                    .build();
                
                productMap.put(chiTietSanPhamId, dto);
            }
            
            // Cộng dồn số lượng từ hoa_don_chi_tiet.so_luong
            BestSellingProductDTO dto = productMap.get(chiTietSanPhamId);
            dto.setSoLuongBan(dto.getSoLuongBan() + hdct.getSoLuong());
        }
        
        System.out.println("📈 [StatisticsService] Step 4: Processing summary");
        System.out.println("   - Total records processed: " + chiTietList.size());
        System.out.println("   - Records skipped: " + skippedCount);
        System.out.println("   - Product groups created: " + productMap.size());
        
        if (productMap.isEmpty()) {
            System.out.println("⚠️ [StatisticsService] No valid products after processing!");
            System.out.println("   All records were skipped. Possible issues:");
            System.out.println("   1. chiTietSanPham relationships are not loaded");
            System.out.println("   2. sanPham relationships are not loaded");
            System.out.println("   3. Data integrity issues in database");
            return new java.util.ArrayList<>();
        }
        
        // Sắp xếp theo số lượng bán giảm dần và lấy top
        List<BestSellingProductDTO> result = productMap.values().stream()
            .sorted((a, b) -> Integer.compare(b.getSoLuongBan(), a.getSoLuongBan()))
            .limit(limit)
            .collect(Collectors.toList());
        
        System.out.println("✅ [StatisticsService] Returning " + result.size() + " best selling products:");
        for (int i = 0; i < result.size(); i++) {
            BestSellingProductDTO dto = result.get(i);
            System.out.println("   " + (i + 1) + ". " + dto.getTenSanPham() + 
                             " | Màu: " + (dto.getMauSac() != null ? dto.getMauSac() : "N/A") + 
                             " | Kiểu: " + (dto.getKieuDang() != null ? dto.getKieuDang() : "N/A") + 
                             " | SL: " + dto.getSoLuongBan() + 
                             " | Giá: " + dto.getDonGia());
        }
        
        return result;
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
        
        LocalDateTime endDate;
        LocalDateTime startDate;
        
        // Xác định khoảng thời gian dựa vào period
        LocalDate today = LocalDate.now();
        switch (period.toLowerCase()) {
            case "day":
            case "today":
                // Hôm nay: từ đầu ngày hôm nay đến hiện tại
                startDate = today.atStartOfDay();
                endDate = LocalDateTime.now();
                break;
            case "week":
                // Tuần này: từ đầu tuần (Thứ 2) đến hiện tại
                startDate = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
                endDate = LocalDateTime.now();
                break;
            case "month":
                // Tháng 11/2025: từ 1/11/2025 đến 1/12/2025
                startDate = LocalDate.of(2025, 11, 1).atStartOfDay();
                endDate = LocalDate.of(2025, 12, 1).atStartOfDay();
                break;
            case "year":
                // Năm 2025: từ 1/1/2025 đến 1/1/2026
                startDate = LocalDate.of(2025, 1, 1).atStartOfDay();
                endDate = LocalDate.of(2026, 1, 1).atStartOfDay();
                break;
            default:
                System.err.println("⚠️ [StatisticsService] Invalid period: " + period + ", defaulting to month");
                startDate = LocalDate.of(2025, 11, 1).atStartOfDay();
                endDate = LocalDate.of(2025, 12, 1).atStartOfDay();
        }
        
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
        
        System.out.println("📊 [StatisticsService] Statistics calculated:");
        System.out.println("   - Doanh thu: " + doanhThu);
        System.out.println("   - Sản phẩm đã bán: " + sanPhamDaBan);
        System.out.println("   - Đơn hàng: " + donHang);
        System.out.println("========================================");
        
        return PeriodStatisticsDTO.builder()
                .doanhThu(doanhThu)
                .sanPhamDaBan(sanPhamDaBan)
                .donHang(donHang)
                .period(period)
                .build();
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
}

