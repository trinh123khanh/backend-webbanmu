package com.example.backend.service.impl;

import com.example.backend.dto.DotGiamGiaRequest;
import com.example.backend.dto.DotGiamGiaResponse;
import com.example.backend.entity.DotGiamGia;
import com.example.backend.repository.DotGiamGiaRepository;
import com.example.backend.service.DotGiamGiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DotGiamGiaServiceImpl implements DotGiamGiaService {

    private final DotGiamGiaRepository dotGiamGiaRepository;

    @Override
    public DotGiamGiaResponse createDotGiamGia(DotGiamGiaRequest request) {
        log.info("Creating new DotGiamGia with ma: {}", request.getMaDotGiamGia());
        
        // Kiểm tra mã đợt giảm giá đã tồn tại chưa
        if (dotGiamGiaRepository.existsByMaDotGiamGia(request.getMaDotGiamGia())) {
            throw new RuntimeException("Mã đợt giảm giá đã tồn tại: " + request.getMaDotGiamGia());
        }
        
        // Kiểm tra ngày bắt đầu và kết thúc
        if (request.getNgayBatDau().isAfter(request.getNgayKetThuc())) {
            throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        
        DotGiamGia dotGiamGia = mapRequestToEntity(request);
        DotGiamGia savedDotGiamGia = dotGiamGiaRepository.save(dotGiamGia);
        
        log.info("Created DotGiamGia with id: {}", savedDotGiamGia.getId());
        return mapEntityToResponse(savedDotGiamGia);
    }

    @Override
    @Transactional(readOnly = true)
    public DotGiamGiaResponse getDotGiamGiaById(Long id) {
        log.info("Getting DotGiamGia by id: {}", id);
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));
        return mapEntityToResponse(dotGiamGia);
    }

    @Override
    @Transactional(readOnly = true)
    public DotGiamGiaResponse getDotGiamGiaByMa(String maDotGiamGia) {
        log.info("Getting DotGiamGia by ma: {}", maDotGiamGia);
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findByMaDotGiamGia(maDotGiamGia)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với mã: " + maDotGiamGia));
        return mapEntityToResponse(dotGiamGia);
    }

    @Override
    public DotGiamGiaResponse updateDotGiamGia(Long id, DotGiamGiaRequest request) {
        log.info("Updating DotGiamGia with id: {}", id);
        
        DotGiamGia existingDotGiamGia = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));
        
        // Kiểm tra mã đợt giảm giá đã tồn tại chưa (trừ id hiện tại)
        if (dotGiamGiaRepository.existsByMaDotGiamGiaAndIdNot(request.getMaDotGiamGia(), id)) {
            throw new RuntimeException("Mã đợt giảm giá đã tồn tại: " + request.getMaDotGiamGia());
        }
        
        // Kiểm tra ngày bắt đầu và kết thúc
        if (request.getNgayBatDau().isAfter(request.getNgayKetThuc())) {
            throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        
        // Cập nhật thông tin
        existingDotGiamGia.setMaDotGiamGia(request.getMaDotGiamGia());
        existingDotGiamGia.setLoaiDotGiamGia(request.getLoaiDotGiamGia());
        existingDotGiamGia.setGiaTriDotGiam(request.getGiaTriDotGiam());
        existingDotGiamGia.setSoTien(request.getSoTien());
        existingDotGiamGia.setMoTa(request.getMoTa());
        existingDotGiamGia.setNgayBatDau(request.getNgayBatDau());
        existingDotGiamGia.setNgayKetThuc(request.getNgayKetThuc());
        existingDotGiamGia.setSoLuongSuDung(request.getSoLuongSuDung());
        existingDotGiamGia.setTenDotGiamGia(request.getTenDotGiamGia());
        existingDotGiamGia.setTrangThai(request.getTrangThai());
        
        DotGiamGia updatedDotGiamGia = dotGiamGiaRepository.save(existingDotGiamGia);
        
        log.info("Updated DotGiamGia with id: {}", updatedDotGiamGia.getId());
        return mapEntityToResponse(updatedDotGiamGia);
    }

    @Override
    public void deleteDotGiamGia(Long id) {
        log.info("Deleting DotGiamGia with id: {}", id);
        
        if (!dotGiamGiaRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id);
        }
        
        dotGiamGiaRepository.deleteById(id);
        log.info("Deleted DotGiamGia with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DotGiamGiaResponse> getAllDotGiamGia(Pageable pageable) {
        log.info("Getting all DotGiamGia with pagination");
        Page<DotGiamGia> dotGiamGiaPage = dotGiamGiaRepository.findAll(pageable);
        return dotGiamGiaPage.map(this::mapEntityToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DotGiamGiaResponse> getAllDotGiamGia() {
        log.info("Getting all DotGiamGia");
        List<DotGiamGia> dotGiamGiaList = dotGiamGiaRepository.findAll();
        return dotGiamGiaList.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DotGiamGiaResponse> searchDotGiamGia(String tenDotGiamGia, String maDotGiamGia, 
                                                     Boolean trangThai, String loaiDotGiamGia, 
                                                     Pageable pageable) {
        log.info("Searching DotGiamGia with filters - tenDotGiamGia: {}, maDotGiamGia: {}, trangThai: {}, loaiDotGiamGia: {}", 
                tenDotGiamGia, maDotGiamGia, trangThai, loaiDotGiamGia);
        
        Page<DotGiamGia> dotGiamGiaPage = dotGiamGiaRepository.searchDotGiamGia(
                tenDotGiamGia, maDotGiamGia, trangThai, loaiDotGiamGia, pageable);
        return dotGiamGiaPage.map(this::mapEntityToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DotGiamGiaResponse> getDotGiamGiaByTrangThai(Boolean trangThai) {
        log.info("Getting DotGiamGia by trangThai: {}", trangThai);
        List<DotGiamGia> dotGiamGiaList = dotGiamGiaRepository.findByTrangThai(trangThai);
        return dotGiamGiaList.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DotGiamGiaResponse> getDotGiamGiaByLoai(String loaiDotGiamGia) {
        log.info("Getting DotGiamGia by loaiDotGiamGia: {}", loaiDotGiamGia);
        List<DotGiamGia> dotGiamGiaList = dotGiamGiaRepository.findByLoaiDotGiamGia(loaiDotGiamGia);
        return dotGiamGiaList.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DotGiamGiaResponse> getActiveDotGiamGia() {
        log.info("Getting active DotGiamGia");
        List<DotGiamGia> dotGiamGiaList = dotGiamGiaRepository.findActiveDiscounts(LocalDateTime.now());
        return dotGiamGiaList.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMaDotGiamGia(String maDotGiamGia) {
        return dotGiamGiaRepository.existsByMaDotGiamGia(maDotGiamGia);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMaDotGiamGiaAndIdNot(String maDotGiamGia, Long id) {
        return dotGiamGiaRepository.existsByMaDotGiamGiaAndIdNot(maDotGiamGia, id);
    }

    // Helper methods
    private DotGiamGia mapRequestToEntity(DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = new DotGiamGia();
        dotGiamGia.setMaDotGiamGia(request.getMaDotGiamGia());
        dotGiamGia.setLoaiDotGiamGia(request.getLoaiDotGiamGia());
        dotGiamGia.setGiaTriDotGiam(request.getGiaTriDotGiam());
        dotGiamGia.setSoTien(request.getSoTien());
        dotGiamGia.setMoTa(request.getMoTa());
        dotGiamGia.setNgayBatDau(request.getNgayBatDau());
        dotGiamGia.setNgayKetThuc(request.getNgayKetThuc());
        dotGiamGia.setSoLuongSuDung(request.getSoLuongSuDung());
        dotGiamGia.setTenDotGiamGia(request.getTenDotGiamGia());
        dotGiamGia.setTrangThai(request.getTrangThai());
        return dotGiamGia;
    }

    private DotGiamGiaResponse mapEntityToResponse(DotGiamGia dotGiamGia) {
        DotGiamGiaResponse response = new DotGiamGiaResponse();
        response.setId(dotGiamGia.getId());
        response.setMaDotGiamGia(dotGiamGia.getMaDotGiamGia());
        response.setLoaiDotGiamGia(dotGiamGia.getLoaiDotGiamGia());
        response.setGiaTriDotGiam(dotGiamGia.getGiaTriDotGiam());
        response.setSoTien(dotGiamGia.getSoTien());
        response.setMoTa(dotGiamGia.getMoTa());
        response.setNgayBatDau(dotGiamGia.getNgayBatDau());
        response.setNgayKetThuc(dotGiamGia.getNgayKetThuc());
        response.setSoLuongSuDung(dotGiamGia.getSoLuongSuDung());
        response.setTenDotGiamGia(dotGiamGia.getTenDotGiamGia());
        response.setTrangThai(dotGiamGia.getTrangThai());
        return response;
    }
}
