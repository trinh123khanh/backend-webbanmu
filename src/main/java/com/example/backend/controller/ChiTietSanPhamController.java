package com.example.backend.controller;

import com.example.backend.dto.ChiTietSanPhamRequest;
import com.example.backend.dto.ChiTietSanPhamResponse;
import com.example.backend.service.ChiTietSanPhamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ChiTietSanPhamController {
    private final ChiTietSanPhamService service;
    public ChiTietSanPhamController(ChiTietSanPhamService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChiTietSanPhamResponse> create(@RequestBody ChiTietSanPhamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChiTietSanPhamResponse> update(@PathVariable Long id, @RequestBody ChiTietSanPhamRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChiTietSanPhamResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<ChiTietSanPhamResponse>> getBySanPhamId(@PathVariable Long sanPhamId) {
        return ResponseEntity.ok(service.getBySanPhamId(sanPhamId));
    }
}
