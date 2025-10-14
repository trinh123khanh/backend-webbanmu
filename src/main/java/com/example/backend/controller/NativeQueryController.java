package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

@RestController
@RequestMapping("/api/native")
@CrossOrigin(origins = "*")
public class NativeQueryController {

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/khach-hang")
    public ResponseEntity<?> getKhachHangNative() {
        try {
            Query query = entityManager.createNativeQuery("SELECT id, ma_khach_hang, ten_khach_hang, email, so_dien_thoai, diem_tich_luy, trang_thai FROM khach_hang LIMIT 5");
            List<Object[]> results = query.getResultList();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi native query: " + e.getMessage());
        }
    }
    
    @GetMapping("/khach-hang-simple")
    public ResponseEntity<?> getKhachHangSimple() {
        try {
            Query query = entityManager.createNativeQuery("SELECT * FROM khach_hang LIMIT 3");
            List<Object[]> results = query.getResultList();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi simple query: " + e.getMessage());
        }
    }
}
