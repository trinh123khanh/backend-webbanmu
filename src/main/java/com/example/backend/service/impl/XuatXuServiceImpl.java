package com.example.backend.service.impl;

import com.example.backend.dto.XuatXuRequest;
import com.example.backend.dto.XuatXuResponse;
import com.example.backend.entity.XuatXu;
import com.example.backend.repository.XuatXuRepository;
import com.example.backend.service.XuatXuService;
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
public class XuatXuServiceImpl implements XuatXuService {

    private final XuatXuRepository repository;

    public XuatXuServiceImpl(XuatXuRepository repository) {
        this.repository = repository;
    }

    @Override
    public XuatXuResponse create(XuatXuRequest request) {
        if (!StringUtils.hasText(request.getTenXuatXu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên xuất xứ là bắt buộc");
        }
        if (repository.existsByTenXuatXu(request.getTenXuatXu())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên xuất xứ đã tồn tại");
        }
        XuatXu e = new XuatXu();
        e.setTenXuatXu(request.getTenXuatXu());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public XuatXuResponse update(Long id, XuatXuRequest request) {
        XuatXu e = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy xuất xứ"));
        if (!StringUtils.hasText(request.getTenXuatXu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên xuất xứ là bắt buộc");
        }
        if (repository.existsByTenXuatXu(request.getTenXuatXu()) && !request.getTenXuatXu().equals(e.getTenXuatXu())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên xuất xứ đã tồn tại");
        }
        e.setTenXuatXu(request.getTenXuatXu());
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
    public XuatXuResponse getById(Long id) {
        return repository.findById(id).map(this::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy xuất xứ"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<XuatXuResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<XuatXuResponse> getAllActive() {
        return repository.findAll().stream()
                .filter(e -> e.getTrangThai() == null || Boolean.TRUE.equals(e.getTrangThai()))
                .map(this::map)
                .collect(Collectors.toList());
    }

    private XuatXuResponse map(XuatXu e) {
        XuatXuResponse r = new XuatXuResponse();
        r.setId(e.getId());
        r.setTenXuatXu(e.getTenXuatXu());
        r.setMoTa(e.getMoTa());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}


