package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db-test")
@CrossOrigin(origins = "*")
public class DatabaseTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/khach-hang-structure")
    public Map<String, Object> getKhachHangStructure() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Lấy cấu trúc bảng khach_hang
            String sql = "SELECT column_name, data_type, is_nullable " +
                       "FROM information_schema.columns " +
                       "WHERE table_name = 'khach_hang' " +
                       "ORDER BY ordinal_position";
            
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
            result.put("success", true);
            result.put("columns", columns);
            result.put("message", "Cấu trúc bảng khach_hang");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/khach-hang-data")
    public Map<String, Object> getKhachHangData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Lấy dữ liệu mẫu từ bảng khach_hang
            String sql = "SELECT id, ma_khach_hang, ten_khach_hang, email, so_dien_thoai " +
                       "FROM khach_hang LIMIT 3";
            
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            result.put("success", true);
            result.put("data", data);
            result.put("count", data.size());
            result.put("message", "Dữ liệu mẫu từ bảng khach_hang");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/test-query")
    public Map<String, Object> testQuery() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test query đơn giản
            String sql = "SELECT COUNT(*) as total FROM khach_hang";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            
            result.put("success", true);
            result.put("total_records", count);
            result.put("message", "Test query thành công");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/test-native-query")
    public Map<String, Object> testNativeQuery() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test native query với tên cột chính xác
            String sql = "SELECT id, ma_khach_hang, ten_khach_hang, email FROM khach_hang LIMIT 3";
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            
            result.put("success", true);
            result.put("data", data);
            result.put("message", "Native query thành công");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
