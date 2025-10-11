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
        // Kiểm tra xem kiểu dáng mũ có tồn tại không
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kiểu dáng mũ");
        }
        
        try {
            // Hard delete: xóa cứng khỏi database
            repository.deleteById(id);
        } catch (Exception e) {
            // Nếu có lỗi foreign key constraint, báo lỗi rõ ràng
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Không thể xóa kiểu dáng mũ này vì đang được sử dụng trong sản phẩm. " +
                    "Vui lòng cập nhật hoặc xóa các sản phẩm liên quan trước.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Lỗi khi xóa kiểu dáng mũ: " + e.getMessage());
        }
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


