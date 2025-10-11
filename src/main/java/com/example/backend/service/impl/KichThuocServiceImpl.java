package com.example.backend.service.impl;

import com.example.backend.dto.KichThuocRequest;
import com.example.backend.dto.KichThuocResponse;
import com.example.backend.entity.KichThuoc;
import com.example.backend.repository.KichThuocRepository;
import com.example.backend.service.KichThuocService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class KichThuocServiceImpl implements KichThuocService {

    private final KichThuocRepository repository;

    public KichThuocServiceImpl(KichThuocRepository repository) {
        this.repository = repository;
    }

    @Override
    public KichThuocResponse create(KichThuocRequest request) {
        if (!StringUtils.hasText(request.getTenKichThuoc())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên kích thước là bắt buộc");
        }
        if (repository.existsByTenKichThuoc(request.getTenKichThuoc())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên kích thước đã tồn tại");
        }
        KichThuoc e = new KichThuoc();
        e.setTenKichThuoc(request.getTenKichThuoc());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public KichThuocResponse update(Long id, KichThuocRequest request) {
        KichThuoc e = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kích thước"));
        if (!StringUtils.hasText(request.getTenKichThuoc())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên kích thước là bắt buộc");
        }
        if (repository.existsByTenKichThuoc(request.getTenKichThuoc()) && !request.getTenKichThuoc().equals(e.getTenKichThuoc())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên kích thước đã tồn tại");
        }
        e.setTenKichThuoc(request.getTenKichThuoc());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public KichThuocResponse getById(Long id) {
        return repository.findById(id).map(this::map).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kích thước"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KichThuocResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    @Override
    public void fixSequence() {
        repository.fixSequence();
    }

    private KichThuocResponse map(KichThuoc e) {
        KichThuocResponse r = new KichThuocResponse();
        r.setId(e.getId());
        r.setTenKichThuoc(e.getTenKichThuoc());
        r.setMoTa(e.getMoTa());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}


