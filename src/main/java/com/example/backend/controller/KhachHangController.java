package com.example.backend.controller;

import com.example.backend.entity.KhachHang;
import com.example.backend.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*")
public class KhachHangController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @GetMapping("/all")
    public ResponseEntity<List<KhachHang>> getAllKhachHang() {
        List<KhachHang> khachHangs = khachHangRepository.findAll();
        return ResponseEntity.ok(khachHangs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHang> getKhachHangById(@PathVariable Long id) {
        Optional<KhachHang> khachHang = khachHangRepository.findById(id);
        if (khachHang.isPresent()) {
            return ResponseEntity.ok(khachHang.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<KhachHang>> searchKhachHangByName(@RequestParam String name) {
        List<KhachHang> khachHangs = khachHangRepository.findByTenKhachHangContainingIgnoreCase(name);
        return ResponseEntity.ok(khachHangs);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<KhachHang> getKhachHangByEmail(@PathVariable String email) {
        Optional<KhachHang> khachHang = khachHangRepository.findByEmail(email);
        if (khachHang.isPresent()) {
            return ResponseEntity.ok(khachHang.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<KhachHang>> getKhachHangByPhone(@PathVariable String phone) {
        List<KhachHang> khachHangs = khachHangRepository.findBySoDienThoai(phone);
        return ResponseEntity.ok(khachHangs);
    }

    @PostMapping("/create")
    public ResponseEntity<KhachHang> createKhachHang(@RequestBody KhachHang khachHang) {
        try {
            // Set default values if not provided
            if (khachHang.getNgayTao() == null) {
                khachHang.setNgayTao(java.time.LocalDate.now());
            }
            if (khachHang.getDiemTichLuy() == null) {
                khachHang.setDiemTichLuy(0);
            }
            if (khachHang.getTrangThai() == null) {
                khachHang.setTrangThai(true);
            }
            if (khachHang.getEmail() == null || khachHang.getEmail().isEmpty()) {
                // Generate email from name if not provided
                String email = khachHang.getTenKhachHang().toLowerCase()
                    .replaceAll("\\s+", "") + "@example.com";
                khachHang.setEmail(email);
            }
            if (khachHang.getSoDienThoai() == null || khachHang.getSoDienThoai().isEmpty()) {
                khachHang.setSoDienThoai("Chưa có");
            }

            KhachHang savedKhachHang = khachHangRepository.save(khachHang);
            return ResponseEntity.ok(savedKhachHang);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
