package com.example.backend.controller;

import com.example.backend.entity.SanPham;
import com.example.backend.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@CrossOrigin(origins = "*")
public class SanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping("/all")
    public ResponseEntity<List<SanPham>> getAllSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findAll();
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SanPham>> getActiveSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findByTrangThai(true);
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/available")
    public ResponseEntity<List<SanPham>> getAvailableSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findAvailableProducts();
        return ResponseEntity.ok(sanPhams);
    }
}
