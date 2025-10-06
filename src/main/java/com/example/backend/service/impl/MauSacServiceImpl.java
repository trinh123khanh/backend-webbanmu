package com.example.backend.service.impl;

import com.example.backend.dto.MauSacRequest;
import com.example.backend.dto.MauSacResponse;
import com.example.backend.entity.MauSac;
import com.example.backend.repository.MauSacRepository;
import com.example.backend.service.MauSacService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class MauSacServiceImpl implements MauSacService {

    private final MauSacRepository repository;

    public MauSacServiceImpl(MauSacRepository repository) {
        this.repository = repository;
    }

    @Override
    public MauSacResponse create(MauSacRequest request) {
        if (!StringUtils.hasText(request.getTenMau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên màu là bắt buộc");
        }
        if (repository.existsByTenMau(request.getTenMau())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên màu đã tồn tại");
        }
        MauSac e = new MauSac();
        e.setTenMau(request.getTenMau());
        e.setMaMau(request.getMaMau());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public MauSacResponse update(Long id, MauSacRequest request) {
        MauSac e = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu sắc"));
        if (!StringUtils.hasText(request.getTenMau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên màu là bắt buộc");
        }
        // Nếu đổi tên sang tên đã tồn tại (khác id hiện tại) thì báo lỗi
        if (repository.existsByTenMau(request.getTenMau()) && !request.getTenMau().equals(e.getTenMau())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên màu đã tồn tại");
        }
        e.setTenMau(request.getTenMau());
        e.setMaMau(request.getMaMau());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MauSacResponse getById(Long id) {
        return repository.findById(id).map(this::map).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu sắc"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MauSacResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    private MauSacResponse map(MauSac e) {
        MauSacResponse r = new MauSacResponse();
        r.setId(e.getId());
        r.setTenMau(e.getTenMau());
        r.setMaMau(e.getMaMau());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}


