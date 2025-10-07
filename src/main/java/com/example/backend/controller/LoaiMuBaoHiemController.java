package com.example.backend.controller;

import com.example.backend.dto.LoaiMuBaoHiemResponse;
import com.example.backend.entity.LoaiMuBaoHiem;
import com.example.backend.repository.LoaiMuBaoHiemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loai-mu-bao-hiem")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class LoaiMuBaoHiemController {

    private final LoaiMuBaoHiemRepository repository;

    public LoaiMuBaoHiemController(LoaiMuBaoHiemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<LoaiMuBaoHiemResponse>> getAllActive() {
        List<LoaiMuBaoHiem> list = repository.findAll();
        List<LoaiMuBaoHiemResponse> result = list.stream()
                .filter(e -> e.getTrangThai() == null || Boolean.TRUE.equals(e.getTrangThai()))
                .map(e -> {
                    LoaiMuBaoHiemResponse r = new LoaiMuBaoHiemResponse();
                    r.setId(e.getId());
                    r.setTenLoai(e.getTenLoai());
                    r.setTrangThai(e.getTrangThai());
                    return r;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}


