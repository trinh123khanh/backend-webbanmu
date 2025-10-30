package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.repository.HoaDonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    private HoaDonDTO toDTO(HoaDon h) {
        return HoaDonDTO.builder()
                .id(h.getId())
                .maHoaDon(h.getMaHoaDon())
                .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                .ngayTao(h.getNgayTao())
                .ngayThanhToan(h.getNgayThanhToan())
                .tongTien(h.getTongTien())
                .tienGiamGia(h.getTienGiamGia())
                .giamGiaPhanTram(h.getGiamGiaPhanTram())
                .thanhTien(h.getThanhTien())
                .ghiChu(h.getGhiChu())
                .trangThai(h.getTrangThai())
                .soLuongSanPham(h.getSoLuongSanPham())
                .build();
    }

    private void updateEntityFromDTO(HoaDon h, HoaDonDTO d) {
        if (d.getMaHoaDon() != null) h.setMaHoaDon(d.getMaHoaDon());
        if (d.getNgayThanhToan() != null) h.setNgayThanhToan(d.getNgayThanhToan());
        if (d.getTongTien() != null) h.setTongTien(d.getTongTien());
        if (d.getTienGiamGia() != null) h.setTienGiamGia(d.getTienGiamGia());
        if (d.getGiamGiaPhanTram() != null) h.setGiamGiaPhanTram(d.getGiamGiaPhanTram());
        if (d.getThanhTien() != null) h.setThanhTien(d.getThanhTien());
        if (d.getGhiChu() != null) h.setGhiChu(d.getGhiChu());
        if (d.getTrangThai() != null) h.setTrangThai(d.getTrangThai());
        if (d.getSoLuongSanPham() != null) h.setSoLuongSanPham(d.getSoLuongSanPham());
    }

    @Transactional
    public HoaDonDTO createHoaDon(HoaDonDTO dto) {
        HoaDon h = new HoaDon();
        h.setMaHoaDon(dto.getMaHoaDon());
        h.setNgayTao(LocalDateTime.now());
        updateEntityFromDTO(h, dto);
        HoaDon saved = hoaDonRepository.save(h);
        return toDTO(saved);
    }

    @Transactional
    public HoaDonDTO updateHoaDon(Long id, HoaDonDTO dto) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        updateEntityFromDTO(h, dto);
        HoaDon saved = hoaDonRepository.save(h);
        return toDTO(saved);
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        hoaDonRepository.delete(h);
    }

    public Page<HoaDon> getAllHoaDon(String keyword, Pageable pageable) {
        Specification<HoaDon> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(root.get("maHoaDon"), "%" + keyword + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return hoaDonRepository.findAll(spec, pageable);
    }

    public Optional<HoaDon> getHoaDonById(Long id) {
        return hoaDonRepository.findById(id);
    }
}
