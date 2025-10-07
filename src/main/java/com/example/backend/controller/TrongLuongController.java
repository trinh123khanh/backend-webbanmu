package com.example.backend.controller;

import com.example.backend.entity.TrongLuong;
import com.example.backend.repository.TrongLuongRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trong-luong")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class TrongLuongController {

    private final TrongLuongRepository repository;

    public TrongLuongController(TrongLuongRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrongLuong>> getAllActive() {
        return ResponseEntity.ok(repository.findAll());
    }
}


