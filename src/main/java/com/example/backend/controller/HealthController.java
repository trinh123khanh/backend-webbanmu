package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String home() {
        return "Backend is running!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "Application is healthy!";
    }

    @GetMapping("/test-db")
    public String testDb() {
        try {
            String sql = "SELECT COUNT(*) FROM khach_hang";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return "Database OK - Khách hàng: " + count;
        } catch (Exception e) {
            return "Database ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/test-connection")
    public String testConnection() {
        try {
            String sql = "SELECT 1 as test";
            Integer test = jdbcTemplate.queryForObject(sql, Integer.class);
            return "Database connection OK - Test value: " + test;
        } catch (Exception e) {
            return "Database connection ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/test-jpa")
    public String testJpa() {
        try {
            String sql = "SELECT id, ten_khach_hang, email FROM khach_hang LIMIT 3";
            var result = jdbcTemplate.queryForList(sql);
            return "JPA Test OK - Data: " + result.toString();
        } catch (Exception e) {
            return "JPA Test ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/check-columns")
    public String checkColumns() {
        try {
            String sql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'khach_hang' ORDER BY ordinal_position";
            var result = jdbcTemplate.queryForList(sql);
            return "Columns: " + result.toString();
        } catch (Exception e) {
            return "Column Check ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/list-columns")
    public String listColumns() {
        try {
            String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = 'khach_hang' ORDER BY ordinal_position";
            var result = jdbcTemplate.queryForList(sql);
            StringBuilder sb = new StringBuilder();
            for (var row : result) {
                sb.append(row.get("column_name")).append(", ");
            }
            return "Column names: " + sb.toString();
        } catch (Exception e) {
            return "List Columns ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/test-diachi")
    public String testDiaChi() {
        try {
            String sql = "SELECT id, khach_hang_id, ten_nguoi_nhan, dia_chi FROM dia_chi_khach_hang LIMIT 3";
            var result = jdbcTemplate.queryForList(sql);
            return "DiaChi Test OK - Data: " + result.toString();
        } catch (Exception e) {
            return "DiaChi Test ERROR: " + e.getMessage();
        }
    }

}