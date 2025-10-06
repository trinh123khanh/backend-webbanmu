package com.example.backend.service.impl;

import com.example.backend.dto.KieuDangMuRequest;
import com.example.backend.dto.KieuDangMuResponse;
import com.example.backend.entity.KieuDangMu;
import com.example.backend.repository.KieuDangMuRepository;
import com.example.backend.service.KieuDangMuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class KieuDangMuServiceImpl implements KieuDangMuService {

    private final KieuDangMuRepository repository;

    public KieuDangMuServiceImpl(KieuDangMuRepository repository) {
        this.repository = repository;
    }

    @Override
    public KieuDangMuResponse create(KieuDangMuRequest request) {
        if (!StringUtils.hasText(request.getTenKieuDang())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên kiểu dáng là bắt buộc");
        }
        if (repository.existsByTenKieuDang(request.getTenKieuDang())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên kiểu dáng đã tồn tại");
        }
        KieuDangMu e = new KieuDangMu();
        e.setTenKieuDang(request.getTenKieuDang());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public KieuDangMuResponse update(Long id, KieuDangMuRequest request) {
        KieuDangMu e = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kiểu dáng"));
        if (!StringUtils.hasText(request.getTenKieuDang())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên kiểu dáng là bắt buộc");
        }
        if (repository.existsByTenKieuDang(request.getTenKieuDang()) && !request.getTenKieuDang().equals(e.getTenKieuDang())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên kiểu dáng đã tồn tại");
        }
        e.setTenKieuDang(request.getTenKieuDang());
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
    public KieuDangMuResponse getById(Long id) {
        return repository.findById(id).map(this::map).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kiểu dáng"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KieuDangMuResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    private KieuDangMuResponse map(KieuDangMu e) {
        KieuDangMuResponse r = new KieuDangMuResponse();
        r.setId(e.getId());
        r.setTenKieuDang(e.getTenKieuDang());
        r.setMoTa(e.getMoTa());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}


