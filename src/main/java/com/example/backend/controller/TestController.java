package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-connection")
    public ResponseEntity<?> testDbConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            // Test connection
            boolean isValid = conn.isValid(5); // 5 seconds timeout
            
            // Get database info
            String dbName = conn.getCatalog();
            String dbUrl = conn.getMetaData().getURL();
            String dbUser = conn.getMetaData().getUserName();
            String dbProduct = conn.getMetaData().getDatabaseProductName();
            String dbVersion = conn.getMetaData().getDatabaseProductVersion();
            
            // Get some sample data
            String query = "SELECT version() AS version, current_database() AS db_name, current_user AS db_user";
            Map<String, Object> dbInfo = jdbcTemplate.queryForMap(query);
            
            response.put("status", "success");
            response.put("connection_valid", isValid);
            response.put("database", dbName);
            response.put("url", dbUrl);
            response.put("user", dbUser);
            response.put("database_product", dbProduct);
            response.put("database_version", dbVersion);
            response.put("query_result", dbInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Failed to connect to database: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
