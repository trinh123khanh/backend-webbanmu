package com.example.backend.controller;

import com.example.backend.service.ScheduledReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/report-test")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ReportTestController {

    private final ScheduledReportService scheduledReportService;

    /**
     * Endpoint để test gửi email báo cáo ngay lập tức
     * GET /api/report-test/send-now
     */
    @GetMapping("/send-now")
    public ResponseEntity<Map<String, Object>> sendTestReport() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("🧪 [ReportTestController] Nhận yêu cầu gửi test email báo cáo");
            scheduledReportService.sendTestReport();
            response.put("success", true);
            response.put("message", "Email báo cáo đã được gửi thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [ReportTestController] Lỗi khi gửi test email: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi khi gửi email: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}

