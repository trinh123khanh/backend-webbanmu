package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quick")
@CrossOrigin(origins = "*")
public class QuickTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public Map<String, Object> quickTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test 1: Kết nối database
            Integer test1 = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("database_connection", "OK");
            
            // Test 2: Kiểm tra bảng khach_hang có tồn tại không
            String checkTable = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'khach_hang'";
            Integer tableExists = jdbcTemplate.queryForObject(checkTable, Integer.class);
            result.put("khach_hang_table_exists", tableExists > 0);
            
            if (tableExists > 0) {
                // Test 3: Lấy cấu trúc cột
                String getColumns = "SELECT column_name FROM information_schema.columns WHERE table_name = 'khach_hang' ORDER BY ordinal_position";
                List<String> columns = jdbcTemplate.queryForList(getColumns, String.class);
                result.put("columns", columns);
                
                // Test 4: Đếm số bản ghi
                try {
                    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM khach_hang", Integer.class);
                    result.put("record_count", count);
                } catch (Exception e) {
                    result.put("record_count_error", e.getMessage());
                }
            }
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
