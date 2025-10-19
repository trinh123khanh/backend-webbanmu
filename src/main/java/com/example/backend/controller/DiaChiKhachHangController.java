package com.example.backend.controller;

import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.service.DiaChiKhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi-khach-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiaChiKhachHangController {
    
    private final DiaChiKhachHangService diaChiKhachHangService;
    
    // Lấy tất cả địa chỉ của khách hàng
    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<DiaChiKhachHangDTO>> getDiaChiByKhachHangId(@PathVariable Long khachHangId) {
        try {
            List<DiaChiKhachHangDTO> diaChiList = diaChiKhachHangService.getDiaChiByKhachHangId(khachHangId);
            return ResponseEntity.ok(diaChiList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Lấy địa chỉ mặc định của khách hàng
    @GetMapping("/khach-hang/{khachHangId}/mac-dinh")
    public ResponseEntity<DiaChiKhachHangDTO> getDiaChiMacDinhByKhachHangId(@PathVariable Long khachHangId) {
        try {
            return diaChiKhachHangService.getDiaChiMacDinhByKhachHangId(khachHangId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<DiaChiKhachHangDTO> createDiaChi(@RequestBody DiaChiKhachHangDTO diaChiDTO) {
        try {
            DiaChiKhachHangDTO savedDiaChi = diaChiKhachHangService.createDiaChi(diaChiDTO);
            return ResponseEntity.ok(savedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<DiaChiKhachHangDTO> updateDiaChi(@PathVariable Long id, @RequestBody DiaChiKhachHangDTO diaChiDTO) {
        try {
            DiaChiKhachHangDTO updatedDiaChi = diaChiKhachHangService.updateDiaChi(id, diaChiDTO);
            return ResponseEntity.ok(updatedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Xóa địa chỉ
    @DeleteMapping("/{id}/khach-hang/{khachHangId}")
    public ResponseEntity<Void> deleteDiaChi(@PathVariable Long id, @PathVariable Long khachHangId) {
        try {
            diaChiKhachHangService.deleteDiaChi(id, khachHangId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Đặt địa chỉ làm mặc định
    @PutMapping("/{id}/khach-hang/{khachHangId}/mac-dinh")
    public ResponseEntity<DiaChiKhachHangDTO> setDiaChiMacDinh(@PathVariable Long id, @PathVariable Long khachHangId) {
        try {
            DiaChiKhachHangDTO updatedDiaChi = diaChiKhachHangService.setDiaChiMacDinh(id, khachHangId);
            return ResponseEntity.ok(updatedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Lấy tất cả địa chỉ (cho hiển thị bảng)
    @GetMapping
    public ResponseEntity<List<DiaChiKhachHangDTO>> getAllDiaChi() {
        try {
            List<DiaChiKhachHangDTO> allDiaChi = diaChiKhachHangService.getAllDiaChi();
            return ResponseEntity.ok(allDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
