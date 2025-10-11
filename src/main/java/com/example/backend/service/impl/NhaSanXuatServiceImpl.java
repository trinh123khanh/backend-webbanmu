package com.example.backend.service.impl;

import com.example.backend.dto.NhaSanXuatRequest;
import com.example.backend.dto.NhaSanXuatResponse;
import com.example.backend.entity.NhaSanXuat;
import com.example.backend.repository.NhaSanXuatRepository;
import com.example.backend.service.NhaSanXuatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NhaSanXuatServiceImpl implements NhaSanXuatService {

    private final NhaSanXuatRepository repository;

    public NhaSanXuatServiceImpl(NhaSanXuatRepository repository) {
        this.repository = repository;
    }

    @Override
    public NhaSanXuatResponse create(NhaSanXuatRequest request) {
        if (repository.existsByTenNhaSanXuat(request.getTen())) {
            throw new IllegalArgumentException("Tên nhà sản xuất đã tồn tại");
        }
        NhaSanXuat entity = new NhaSanXuat();
        entity.setTenNhaSanXuat(request.getTen());
        entity.setMoTa(request.getMoTa());
        entity.setQuocGia(request.getQuocGia());
        entity.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(entity));
    }

    @Override
    public NhaSanXuatResponse update(Long id, NhaSanXuatRequest request) {
        NhaSanXuat entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà sản xuất"));
        entity.setTenNhaSanXuat(request.getTen());
        entity.setMoTa(request.getMoTa());
        entity.setQuocGia(request.getQuocGia());
        entity.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        // Kiểm tra xem nhà sản xuất có tồn tại không
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà sản xuất");
        }
        
        try {
            // Hard delete: xóa cứng khỏi database
            repository.deleteById(id);
        } catch (Exception e) {
            // Nếu có lỗi foreign key constraint, báo lỗi rõ ràng
            if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Không thể xóa nhà sản xuất này vì đang được sử dụng trong sản phẩm. " +
                    "Vui lòng cập nhật hoặc xóa các sản phẩm liên quan trước.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Lỗi khi xóa nhà sản xuất: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NhaSanXuatResponse getById(Long id) {
        return repository.findById(id).map(this::map)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà sản xuất"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NhaSanXuatResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    private NhaSanXuatResponse map(NhaSanXuat e) {
        NhaSanXuatResponse r = new NhaSanXuatResponse();
        r.setId(e.getId());
        r.setTen(e.getTenNhaSanXuat());
        r.setMoTa(e.getMoTa());
        r.setTrangThai(Boolean.TRUE.equals(e.getTrangThai()));
        r.setQuocGia(e.getQuocGia());
        return r;
    }
}


