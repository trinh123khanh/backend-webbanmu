package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class ExcelExportController {

    @Autowired
    private ExcelExportService excelExportService;

    @PostMapping("/export-excel")
    public ResponseEntity<ApiResponse<String>> exportToExcel(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) request.get("data");
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không có dữ liệu để xuất Excel", null));
            }

            String base64Excel = excelExportService.exportPhieuGiamGiaToExcel(data);
            
            return ResponseEntity.ok()
                .body(new ApiResponse<>(true, "Xuất Excel thành công", base64Excel));
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Lỗi khi xuất Excel: " + e.getMessage(), null));
        }
    }
}
