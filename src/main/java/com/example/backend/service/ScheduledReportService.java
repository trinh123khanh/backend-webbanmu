package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportService {

    private final StatisticsService statisticsService;
    private final EmailService emailService;
    
    private static final String REPORT_EMAIL = "thanglvph48864@gmail.com";

    /**
     * Chạy vào 19h00 mỗi ngày để gửi báo cáo
     */
    @Scheduled(cron = "0 0 19 * * ?") // Chạy vào 19h00 mỗi ngày
    public void checkAndSendReports() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        log.info("🕐 [ScheduledReport] Đã đến 19h00, kiểm tra ngày cuối kỳ... (Ngày: {})", today);
        
        // Kiểm tra và gửi báo cáo cuối ngày (luôn gửi mỗi ngày)
        try {
            sendDailyReport(today);
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối ngày: {}", e.getMessage(), e);
        }
        
        // Kiểm tra và gửi báo cáo cuối tuần
        if (isEndOfWeek(today)) {
            try {
                sendWeeklyReport(today);
            } catch (Exception e) {
                log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối tuần: {}", e.getMessage(), e);
            }
        }
        
        // Kiểm tra và gửi báo cáo cuối tháng
        if (isEndOfMonth(today)) {
            try {
                sendMonthlyReport(today);
            } catch (Exception e) {
                log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối tháng: {}", e.getMessage(), e);
            }
        }
        
        // Kiểm tra và gửi báo cáo cuối quý
        if (isEndOfQuarter(today)) {
            try {
                sendQuarterlyReport(today);
            } catch (Exception e) {
                log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối quý: {}", e.getMessage(), e);
            }
        }
        
        // Kiểm tra và gửi báo cáo cuối năm
        if (isEndOfYear(today)) {
            try {
                sendYearlyReport(today);
            } catch (Exception e) {
                log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối năm: {}", e.getMessage(), e);
            }
        }
        
        log.info("✅ [ScheduledReport] Hoàn thành kiểm tra và gửi báo cáo");
    }
    
    /**
     * Method test để gửi email ngay lập tức (dùng để test)
     */
    public void sendTestReport() {
        log.info("🧪 [ScheduledReport] Gửi test email báo cáo...");
        LocalDate today = LocalDate.now();
        try {
            sendDailyReport(today);
            log.info("✅ [ScheduledReport] Test email đã được gửi");
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi test email: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isEndOfWeek(LocalDate date) {
        // Chủ nhật là cuối tuần
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private boolean isEndOfMonth(LocalDate date) {
        // Ngày cuối cùng của tháng
        return date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
    }

    private boolean isEndOfQuarter(LocalDate date) {
        int month = date.getMonthValue();
        // Tháng cuối của quý: 3, 6, 9, 12
        boolean isLastMonthOfQuarter = (month == 3 || month == 6 || month == 9 || month == 12);
        return isLastMonthOfQuarter && isEndOfMonth(date);
    }

    private boolean isEndOfYear(LocalDate date) {
        // 31/12 là cuối năm
        return date.getMonthValue() == 12 && date.getDayOfMonth() == 31;
    }

    private void sendDailyReport(LocalDate date) {
        try {
            log.info("📊 [ScheduledReport] Gửi báo cáo cuối ngày: {}", date);
            var statistics = statisticsService.getPeriodStatistics("day");
            String reportHtml = formatStatisticsReport("Ngày", date, statistics);
            emailService.sendStatisticsReport(REPORT_EMAIL, "Báo Cáo Ngày " + formatDate(date), reportHtml);
            log.info("✅ [ScheduledReport] Đã gửi báo cáo cuối ngày thành công");
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối ngày: {}", e.getMessage(), e);
        }
    }

    private void sendWeeklyReport(LocalDate date) {
        try {
            log.info("📊 [ScheduledReport] Gửi báo cáo cuối tuần: {}", date);
            var statistics = statisticsService.getPeriodStatistics("week");
            String reportHtml = formatStatisticsReport("Tuần", date, statistics);
            emailService.sendStatisticsReport(REPORT_EMAIL, "Báo Cáo Tuần " + formatDate(date), reportHtml);
            log.info("✅ [ScheduledReport] Đã gửi báo cáo cuối tuần thành công");
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối tuần: {}", e.getMessage(), e);
        }
    }

    private void sendMonthlyReport(LocalDate date) {
        try {
            log.info("📊 [ScheduledReport] Gửi báo cáo cuối tháng: {}", date);
            var statistics = statisticsService.getPeriodStatistics("month");
            String reportHtml = formatStatisticsReport("Tháng", date, statistics);
            emailService.sendStatisticsReport(REPORT_EMAIL, "Báo Cáo Tháng " + formatDate(date), reportHtml);
            log.info("✅ [ScheduledReport] Đã gửi báo cáo cuối tháng thành công");
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối tháng: {}", e.getMessage(), e);
        }
    }

    private void sendQuarterlyReport(LocalDate date) {
        try {
            log.info("📊 [ScheduledReport] Gửi báo cáo cuối quý: {}", date);
            var statistics = statisticsService.getPeriodStatistics("quarter");
            String reportHtml = formatStatisticsReport("Quý", date, statistics);
            emailService.sendStatisticsReport(REPORT_EMAIL, "Báo Cáo Quý " + getQuarter(date), reportHtml);
            log.info("✅ [ScheduledReport] Đã gửi báo cáo cuối quý thành công");
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối quý: {}", e.getMessage(), e);
        }
    }

    private void sendYearlyReport(LocalDate date) {
        try {
            log.info("📊 [ScheduledReport] Gửi báo cáo cuối năm: {}", date);
            var statistics = statisticsService.getPeriodStatistics("year");
            String reportHtml = formatStatisticsReport("Năm", date, statistics);
            emailService.sendStatisticsReport(REPORT_EMAIL, "Báo Cáo Năm " + date.getYear(), reportHtml);
            log.info("✅ [ScheduledReport] Đã gửi báo cáo cuối năm thành công");
        } catch (Exception e) {
            log.error("❌ [ScheduledReport] Lỗi khi gửi báo cáo cuối năm: {}", e.getMessage(), e);
        }
    }

    private String formatStatisticsReport(String periodType, LocalDate date, com.example.backend.dto.PeriodStatisticsDTO statistics) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #4CAF50; color: white; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".highlight { font-weight: bold; color: #2196F3; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h2>Báo Cáo Thống Kê ").append(periodType).append(" - ").append(formatDate(date)).append("</h2>");
        html.append("<table>");
        html.append("<tr><th>Chỉ tiêu</th><th>Giá trị</th></tr>");
        html.append("<tr><td>Tổng Đơn Hàng</td><td class='highlight'>").append(statistics.getDonHang()).append("</td></tr>");
        html.append("<tr><td>Sản Phẩm Đã Bán</td><td class='highlight'>").append(statistics.getSanPhamDaBan()).append("</td></tr>");
        html.append("<tr><td>Tổng Tiền (Trước giảm giá)</td><td class='highlight'>").append(formatCurrency(statistics.getTongTien())).append("</td></tr>");
        html.append("<tr><td>Tiền Giảm Giá</td><td class='highlight'>").append(formatCurrency(statistics.getTienGiamGia())).append("</td></tr>");
        html.append("<tr><td>Thực Thu (Doanh Thu)</td><td class='highlight'>").append(formatCurrency(statistics.getDoanhThu())).append("</td></tr>");
        html.append("<tr><td>Thu Thực Tế (Đã thanh toán)</td><td class='highlight'>").append(formatCurrency(statistics.getActualRevenue())).append("</td></tr>");
        html.append("<tr><td>Dư Nợ</td><td class='highlight'>").append(formatCurrency(statistics.getDebtRevenue())).append("</td></tr>");
        html.append("</table>");
        html.append("<p><em>Báo cáo được tạo tự động vào ").append(LocalDateTime.now().toString()).append("</em></p>");
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }

    private String formatDate(LocalDate date) {
        return String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", amount.doubleValue());
    }

    private String getQuarter(LocalDate date) {
        int month = date.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        return "Q" + quarter + " " + date.getYear();
    }
}

