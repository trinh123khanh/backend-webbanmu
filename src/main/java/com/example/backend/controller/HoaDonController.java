package com.example.backend.controller;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*") // For development, allow all origins
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    // GET all invoices (unpaginated)
    @GetMapping
    public ResponseEntity<List<HoaDonDTO>> getAllHoaDon() {
        List<HoaDonDTO> hoaDons = hoaDonService.getAllHoaDon();
        return ResponseEntity.ok(hoaDons);
    }

    // GET invoices with pagination, filtering, and sorting
    @GetMapping("/paginated")
    public ResponseEntity<Page<HoaDonDTO>> getHoaDonPaginated(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ngayTao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<HoaDonDTO> hoaDonPage = hoaDonService.getHoaDonPaginated(search, trangThai, page, size, sortBy, sortDir);
        return ResponseEntity.ok(hoaDonPage);
    }

    // GET a single invoice by ID
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getHoaDonById(@PathVariable Long id) {
        Optional<HoaDonDTO> hoaDonDTO = hoaDonService.getHoaDonById(id);
        return hoaDonDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST a new invoice
    @PostMapping
    public ResponseEntity<HoaDonDTO> createHoaDon(@RequestBody HoaDonDTO hoaDonDTO) {
        HoaDonDTO createdHoaDon = hoaDonService.createHoaDon(hoaDonDTO);
        return new ResponseEntity<>(createdHoaDon, HttpStatus.CREATED);
    }

    // PUT to update an invoice
    @PutMapping("/{id}")
    public ResponseEntity<HoaDonDTO> updateHoaDon(@PathVariable Long id, @RequestBody HoaDonDTO hoaDonDTO) {
        return hoaDonService.updateHoaDon(id, hoaDonDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE an invoice
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoaDon(@PathVariable Long id) {
        hoaDonService.deleteHoaDon(id);
        return ResponseEntity.noContent().build();
    }
}
