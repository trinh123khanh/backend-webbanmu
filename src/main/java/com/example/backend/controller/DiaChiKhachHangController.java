package com.example.backend.controller;

import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.repository.DiaChiKhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dia-chi-khach-hang")
@CrossOrigin(origins = "*")
public class DiaChiKhachHangController {

    @Autowired
    private DiaChiKhachHangRepository diaChiKhachHangRepository;

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<DiaChiKhachHangDTO>> getDiaChiByKhachHangId(@PathVariable Long khachHangId) {
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findByKhachHangId(khachHangId);
        List<DiaChiKhachHangDTO> dtoList = danhSachDiaChi.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/khach-hang/{khachHangId}/mac-dinh")
    public ResponseEntity<DiaChiKhachHangDTO> getDiaChiMacDinhByKhachHangId(@PathVariable Long khachHangId) {
        List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHangId);
        if (!diaChiMacDinhList.isEmpty()) {
            return ResponseEntity.ok(convertToDTO(diaChiMacDinhList.get(0)));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<DiaChiKhachHangDTO>> getAllDiaChi() {
        List<DiaChiKhachHang> allDiaChi = diaChiKhachHangRepository.findAll();
        List<DiaChiKhachHangDTO> dtoList = allDiaChi.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    private DiaChiKhachHangDTO convertToDTO(DiaChiKhachHang entity) {
        return DiaChiKhachHangDTO.builder()
                .id(entity.getId())
                .khachHangId(entity.getKhachHang().getId())
                .tenNguoiNhan(entity.getTenNguoiNhan())
                .soDienThoai(entity.getSoDienThoai())
                .diaChi(entity.getDiaChi())
                .tinhThanh(entity.getTinhThanh())
                .quanHuyen(entity.getQuanHuyen())
                .phuongXa(entity.getPhuongXa())
                .macDinh(entity.getMacDinh())
                .trangThai(entity.getTrangThai())
                .build();
    }
}
