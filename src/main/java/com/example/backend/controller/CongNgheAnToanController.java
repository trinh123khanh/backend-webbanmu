package com.example.backend.controller;

import com.example.backend.entity.CongNgheAnToan;
import com.example.backend.repository.CongNgheAnToanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cong-nghe-an-toan")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class CongNgheAnToanController {

    private final CongNgheAnToanRepository repository;

    public CongNgheAnToanController(CongNgheAnToanRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<CongNgheAnToan>> getAllActive() {
        return ResponseEntity.ok(repository.findAll());
    }
}


