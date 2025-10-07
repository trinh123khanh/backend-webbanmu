package com.example.backend.controller;

import com.example.backend.dto.XuatXuRequest;
import com.example.backend.dto.XuatXuResponse;
import com.example.backend.service.XuatXuService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/xuat-xu")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class XuatXuController {

    private final XuatXuService service;

    public XuatXuController(XuatXuService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<XuatXuResponse> create(@Valid @RequestBody XuatXuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<XuatXuResponse> update(@PathVariable Long id, @Valid @RequestBody XuatXuRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<XuatXuResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<XuatXuResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(service.search(keyword, trangThai, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<XuatXuResponse>> getAllActive() {
        return ResponseEntity.ok(service.getAllActive());
    }
}


