package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-db")
@CrossOrigin(origins = "*")
public class SimpleDbController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test kết nối database
            String sql = "SELECT 1 as test";
            Integer test = jdbcTemplate.queryForObject(sql, Integer.class);
            
            result.put("success", true);
            result.put("database_connection", "OK");
            result.put("test_value", test);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/khach-hang-count")
    public Map<String, Object> getKhachHangCount() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String sql = "SELECT COUNT(*) FROM khach_hang";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            
            result.put("success", true);
            result.put("count", count);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/khach-hang-sample")
    public Map<String, Object> getKhachHangSample() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String sql = "SELECT id, ten_khach_hang, email FROM khach_hang LIMIT 3";
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            
            result.put("success", true);
            result.put("data", data);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
