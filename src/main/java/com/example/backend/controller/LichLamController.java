package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.LichLamRequest;
import com.example.backend.dto.LichLamResponse;
import com.example.backend.dto.LichLamWeekResponse;
import com.example.backend.service.LichLamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lich-lam")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@RequiredArgsConstructor
@Slf4j
public class LichLamController {

    private final LichLamService lichLamService;

    /**
     * Test endpoint để kiểm tra controller có hoạt động không
     * GET /api/lich-lam/test
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("LichLamController is working!", "OK"));
    }

    /**
     * Lấy lịch làm theo tuần và năm
     * GET /api/lich-lam/week?week=46&year=2025
     */
    @GetMapping("/week")
    public ResponseEntity<ApiResponse<LichLamWeekResponse>> getLichLamByWeek(
            @RequestParam Integer week,
            @RequestParam Integer year) {
        try {
            LichLamWeekResponse response = lichLamService.getLichLamByWeek(week, year);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch làm thành công", response));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch làm: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy lịch làm: " + e.getMessage()));
        }
    }

    /**
     * Lưu lịch làm cho một nhân viên trong tuần
     * POST /api/lich-lam/save
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Void>> saveLichLam(@RequestBody LichLamRequest request) {
        try {
            lichLamService.saveLichLam(request);
            return ResponseEntity.ok(ApiResponse.success("Lưu lịch làm thành công", null));
        } catch (Exception e) {
            log.error("Lỗi khi lưu lịch làm: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lưu lịch làm: " + e.getMessage()));
        }
    }

    /**
     * Lưu lịch làm cho nhiều nhân viên trong tuần
     * POST /api/lich-lam/save-batch
     */
    @PostMapping("/save-batch")
    public ResponseEntity<ApiResponse<Void>> saveLichLamBatch(@RequestBody List<LichLamRequest> requests) {
        try {
            log.info("Received save-batch request with {} items", requests.size());
            for (LichLamRequest request : requests) {
                log.info("Processing request for userId: {}, week: {}, year: {}, position: {}, caLamList size: {}", 
                    request.getUserId(), request.getWeek(), request.getYear(), 
                    request.getPosition(), request.getCaLamList() != null ? request.getCaLamList().size() : 0);
                lichLamService.saveLichLam(request);
            }
            log.info("Successfully saved all lich lam requests");
            return ResponseEntity.ok(ApiResponse.success("Lưu lịch làm thành công", null));
        } catch (Exception e) {
            log.error("Lỗi khi lưu lịch làm: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lưu lịch làm: " + e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử ca làm
     * GET /api/lich-lam/history?userId=1 (optional)
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LichLamResponse>>> getLichSuCaLam(
            @RequestParam(required = false) Long userId) {
        try {
            List<LichLamResponse> response = lichLamService.getLichSuCaLam(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử ca làm thành công", response));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử ca làm: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy lịch sử ca làm: " + e.getMessage()));
        }
    }
}

