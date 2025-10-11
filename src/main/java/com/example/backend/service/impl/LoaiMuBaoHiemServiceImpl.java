package com.example.backend.service.impl;

import com.example.backend.dto.LoaiMuBaoHiemRequest;
import com.example.backend.dto.LoaiMuBaoHiemResponse;
import com.example.backend.entity.LoaiMuBaoHiem;
import com.example.backend.repository.LoaiMuBaoHiemRepository;
import com.example.backend.service.LoaiMuBaoHiemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoaiMuBaoHiemServiceImpl implements LoaiMuBaoHiemService {

    private final LoaiMuBaoHiemRepository repository;

    public LoaiMuBaoHiemServiceImpl(LoaiMuBaoHiemRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoaiMuBaoHiemResponse create(LoaiMuBaoHiemRequest request) {
        if (!StringUtils.hasText(request.getTenLoai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên loại mũ bảo hiểm là bắt buộc");
        }
        if (repository.existsByTenLoaiIgnoreCase(request.getTenLoai())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên loại mũ bảo hiểm đã tồn tại");
        }

        LoaiMuBaoHiem entity = new LoaiMuBaoHiem();
        entity.setTenLoai(request.getTenLoai());
        entity.setMoTa(request.getMoTa());
        entity.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));

        return map(repository.save(entity));
    }

    @Override
    public LoaiMuBaoHiemResponse update(Long id, LoaiMuBaoHiemRequest request) {
        LoaiMuBaoHiem entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại mũ bảo hiểm"));

        if (!StringUtils.hasText(request.getTenLoai())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên loại mũ bảo hiểm là bắt buộc");
        }

        // Check for duplicate name, excluding the current entity itself
        if (repository.existsByTenLoaiIgnoreCase(request.getTenLoai()) &&
            !repository.findByTenLoaiIgnoreCase(request.getTenLoai())
                       .map(LoaiMuBaoHiem::getId)
                       .filter(currentId -> currentId.equals(id))
                       .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên loại mũ bảo hiểm đã tồn tại");
        }

        entity.setTenLoai(request.getTenLoai());
        entity.setMoTa(request.getMoTa());
        entity.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));

        return map(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại mũ bảo hiểm");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public LoaiMuBaoHiemResponse getById(Long id) {
        return repository.findById(id)
                .map(this::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại mũ bảo hiểm"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoaiMuBaoHiemResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoaiMuBaoHiemResponse> getAllActive() {
        return repository.findByTrangThaiTrue().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private LoaiMuBaoHiemResponse map(LoaiMuBaoHiem entity) {
        LoaiMuBaoHiemResponse response = new LoaiMuBaoHiemResponse();
        response.setId(entity.getId());
        response.setTenLoai(entity.getTenLoai());
        response.setMoTa(entity.getMoTa());
        response.setTrangThai(entity.getTrangThai());
        return response;
    }
}
