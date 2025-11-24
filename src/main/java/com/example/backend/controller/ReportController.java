package com.example.backend.controller;

import com.example.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final EmailService emailService;
    private static final String REPORT_EMAIL = "thanglvph48864@gmail.com";

    /**
     * Test endpoint để kiểm tra controller có hoạt động không
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "ReportController is working!");
        log.info("✅ [ReportController] Test endpoint called");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, Object>> sendReportByEmail(@RequestBody Map<String, Object> reportData) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("📧 [ReportController] Nhận yêu cầu gửi email báo cáo");
            log.info("📧 [ReportController] Report data keys: {}", reportData.keySet());
            
            String periodLabel = (String) reportData.get("periodLabel");
            String reportDate = (String) reportData.get("reportDate");
            String period = (String) reportData.get("period");
            
            // Format HTML báo cáo
            String htmlContent = formatReportHtml(reportData, period);
            
            // Subject email
            String subject = "Báo Cáo " + periodLabel + " - " + reportDate;
            
            // Gửi email
            emailService.sendStatisticsReport(REPORT_EMAIL, subject, htmlContent);
            
            response.put("success", true);
            response.put("message", "Email báo cáo đã được gửi thành công!");
            log.info("✅ [ReportController] Email báo cáo đã được gửi thành công");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [ReportController] Lỗi khi gửi email báo cáo: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi khi gửi email: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String formatReportHtml(Map<String, Object> reportData, String period) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append(".container { background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.append("h2 { color: #2196F3; border-bottom: 2px solid #2196F3; padding-bottom: 10px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; font-size: 14px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }");
        html.append("th { background-color: #4CAF50; color: white; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append("tr:hover { background-color: #e8f5e9; }");
        html.append(".summary-row { background-color: #e3f2fd !important; font-weight: bold; }");
        html.append(".highlight { font-weight: bold; color: #2196F3; }");
        html.append(".footer { margin-top: 20px; padding-top: 10px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        
        // Header
        String periodLabel = (String) reportData.get("periodLabel");
        String reportDate = (String) reportData.get("reportDate");
        html.append("<h2>Báo Cáo Thống Kê ").append(periodLabel).append(" - ").append(reportDate).append("</h2>");
        
        // Tổng quan
        html.append("<h3>Tổng Quan</h3>");
        html.append("<table>");
        html.append("<tr><th>Chỉ tiêu</th><th>Giá trị</th></tr>");
        html.append("<tr><td>Tổng Đơn Hàng</td><td class='highlight'>").append(reportData.get("totalOrders")).append("</td></tr>");
        html.append("<tr><td>Sản Phẩm Đã Bán</td><td class='highlight'>").append(reportData.get("totalProductsSold")).append("</td></tr>");
        html.append("<tr><td>Tổng Tiền (Trước giảm giá)</td><td class='highlight'>").append(formatCurrency(getNumber(reportData.get("totalTongTien")))).append("</td></tr>");
        html.append("<tr><td>Tiền Giảm Giá</td><td class='highlight'>").append(formatCurrency(getNumber(reportData.get("totalTienGiamGia")))).append("</td></tr>");
        html.append("<tr><td>Thực Thu (Doanh Thu)</td><td class='highlight'>").append(formatCurrency(getNumber(reportData.get("totalRevenue")))).append("</td></tr>");
        html.append("<tr><td>Thu Thực Tế (Đã thanh toán)</td><td class='highlight'>").append(formatCurrency(getNumber(reportData.get("totalActualRevenue")))).append("</td></tr>");
        html.append("<tr><td>Dư Nợ</td><td class='highlight'>").append(formatCurrency(getNumber(reportData.get("totalDebtRevenue")))).append("</td></tr>");
        html.append("</table>");
        
        // Chi tiết đơn hàng
        html.append("<h3>Chi Tiết Đơn Hàng</h3>");
        html.append("<table>");
        html.append("<tr><th>Chỉ tiêu</th><th>Giá trị</th></tr>");
        html.append("<tr><td>Đơn Online</td><td>").append(reportData.get("totalOnlineOrders")).append("</td></tr>");
        html.append("<tr><td>Đơn Offline</td><td>").append(reportData.get("totalOfflineOrders")).append("</td></tr>");
        html.append("<tr><td>Đơn Thành Công</td><td>").append(reportData.get("totalSuccessfulOrders")).append("</td></tr>");
        html.append("<tr><td>Đơn Thất Bại</td><td>").append(reportData.get("totalFailedOrders")).append("</td></tr>");
        html.append("<tr><td>Khách Hàng Mới</td><td>").append(reportData.get("totalNewCustomers")).append("</td></tr>");
        html.append("<tr><td>Khách Hàng Quay Lại</td><td>").append(reportData.get("totalReturningCustomers")).append("</td></tr>");
        html.append("<tr><td>Lượt Giảm Giá</td><td>").append(reportData.get("totalDiscountCount")).append("</td></tr>");
        html.append("</table>");
        
        // Bảng chi tiết theo thời gian
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tableData = (List<Map<String, Object>>) reportData.get("tableData");
        if (tableData != null && !tableData.isEmpty()) {
            html.append("<h3>Chi Tiết Theo Thời Gian</h3>");
            html.append("<table>");
            html.append("<tr>");
            html.append("<th>Thời gian</th>");
            html.append("<th>Tổng Đơn Hàng</th>");
            html.append("<th>Đơn Online</th>");
            html.append("<th>Đơn Offline</th>");
            html.append("<th>Đơn thành công</th>");
            html.append("<th>Đơn Thất Bại</th>");
            html.append("<th>Số Sản Phẩm Đã Bán</th>");
            html.append("<th>Khách Hàng Mới</th>");
            html.append("<th>Khách hàng Quay Lại</th>");
            html.append("<th>Lượt Giảm Giá</th>");
            html.append("<th>Tổng</th>");
            html.append("<th>Tiền Giảm</th>");
            html.append("<th>Thực Thu</th>");
            html.append("<th>Thu Thực tế</th>");
            html.append("<th>Dư nợ</th>");
            if (period != null && (period.equals("quarter") || period.equals("year"))) {
                html.append("<th>Tăng trưởng cùng kỳ</th>");
            }
            html.append("</tr>");
            
            for (Map<String, Object> row : tableData) {
                html.append("<tr>");
                html.append("<td>").append(row.get("period")).append("</td>");
                html.append("<td>").append(row.get("totalOrders")).append("</td>");
                html.append("<td>").append(row.get("onlineOrders")).append("</td>");
                html.append("<td>").append(row.get("offlineOrders")).append("</td>");
                html.append("<td>").append(row.get("successfulOrders")).append("</td>");
                html.append("<td>").append(row.get("failedOrders")).append("</td>");
                html.append("<td>").append(row.get("productsSold")).append("</td>");
                html.append("<td>").append(row.get("newCustomers")).append("</td>");
                html.append("<td>").append(row.get("returningCustomers")).append("</td>");
                html.append("<td>").append(row.get("discountCount")).append("</td>");
                html.append("<td>").append(formatCurrency(getNumber(row.get("tongTien")))).append("</td>");
                html.append("<td>").append(formatCurrency(getNumber(row.get("tienGiamGia")))).append("</td>");
                html.append("<td>").append(formatCurrency(getNumber(row.get("revenue")))).append("</td>");
                html.append("<td>").append(formatCurrency(getNumber(row.get("actualRevenue")))).append("</td>");
                html.append("<td>").append(formatCurrency(getNumber(row.get("debt")))).append("</td>");
                if (period != null && (period.equals("quarter") || period.equals("year"))) {
                    Object growth = row.get("growthPercentage");
                    html.append("<td>").append(growth != null ? growth + "%" : "-").append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p><em>Báo cáo được tạo tự động vào ").append(LocalDateTime.now().toString()).append("</em></p>");
        html.append("<p><em>TDK Store - Bán mũ bảo hiểm</em></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }

    private Number getNumber(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String formatCurrency(Number amount) {
        if (amount == null) {
            return "0 ₫";
        }
        double value = amount.doubleValue();
        return String.format("%,.0f ₫", value);
    }
}

