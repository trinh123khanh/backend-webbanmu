package com.example.backend.service.impl;

import com.example.backend.dto.CongNgheAnToanRequest;
import com.example.backend.dto.CongNgheAnToanResponse;
import com.example.backend.entity.CongNgheAnToan;
import com.example.backend.repository.CongNgheAnToanRepository;
import com.example.backend.service.CongNgheAnToanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CongNgheAnToanServiceImpl implements CongNgheAnToanService {

    private final CongNgheAnToanRepository repository;

    public CongNgheAnToanServiceImpl(CongNgheAnToanRepository repository) {
        this.repository = repository;
    }

    @Override
    public CongNgheAnToanResponse create(CongNgheAnToanRequest request) {
        if (!StringUtils.hasText(request.getTenCongNghe())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên công nghệ an toàn là bắt buộc");
        }
        if (repository.existsByTenCongNghe(request.getTenCongNghe())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên công nghệ an toàn đã tồn tại");
        }
        CongNgheAnToan e = new CongNgheAnToan();
        e.setTenCongNghe(request.getTenCongNghe());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public CongNgheAnToanResponse update(Long id, CongNgheAnToanRequest request) {
        CongNgheAnToan e = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy công nghệ an toàn"));
        if (!StringUtils.hasText(request.getTenCongNghe())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên công nghệ an toàn là bắt buộc");
        }
        if (repository.existsByTenCongNghe(request.getTenCongNghe()) && !request.getTenCongNghe().equals(e.getTenCongNghe())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên công nghệ an toàn đã tồn tại");
        }
        e.setTenCongNghe(request.getTenCongNghe());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public void delete(Long id) {
        // Kiểm tra xem công nghệ an toàn có tồn tại không
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy công nghệ an toàn");
        }
        
        try {
            // Hard delete: xóa cứng khỏi database
            repository.deleteById(id);
        } catch (Exception e) {
            // Nếu có lỗi foreign key constraint, báo lỗi rõ ràng
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Không thể xóa công nghệ an toàn này vì đang được sử dụng trong sản phẩm. " +
                    "Vui lòng cập nhật hoặc xóa các sản phẩm liên quan trước.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Lỗi khi xóa công nghệ an toàn: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CongNgheAnToanResponse getById(Long id) {
        return repository.findById(id).map(this::map).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy công nghệ an toàn"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CongNgheAnToanResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    @Override
    public void fixSequence() {
        repository.fixSequence();
    }

    private CongNgheAnToanResponse map(CongNgheAnToan e) {
        CongNgheAnToanResponse r = new CongNgheAnToanResponse();
        r.setId(e.getId());
        r.setTenCongNghe(e.getTenCongNghe());
        r.setMoTa(e.getMoTa());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}
