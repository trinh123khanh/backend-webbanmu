package com.example.backend.service.impl;

import com.example.backend.dto.TrongLuongRequest;
import com.example.backend.dto.TrongLuongResponse;
import com.example.backend.entity.TrongLuong;
import com.example.backend.repository.TrongLuongRepository;
import com.example.backend.service.TrongLuongService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrongLuongServiceImpl implements TrongLuongService {

    private final TrongLuongRepository repository;

    public TrongLuongServiceImpl(TrongLuongRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrongLuongResponse create(TrongLuongRequest request) {
        if (request.getGiaTriTrongLuong() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá trị trọng lượng là bắt buộc");
        }
        if (!StringUtils.hasText(request.getDonVi())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn vị là bắt buộc");
        }
        if (repository.existsByGiaTriTrongLuongAndDonVi(request.getGiaTriTrongLuong(), request.getDonVi())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trọng lượng với giá trị và đơn vị này đã tồn tại");
        }
        
        TrongLuong entity = new TrongLuong();
        entity.setGiaTriTrongLuong(request.getGiaTriTrongLuong());
        entity.setDonVi(request.getDonVi());
        entity.setMoTa(request.getMoTa());
        entity.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        
        return map(repository.save(entity));
    }

    @Override
    public TrongLuongResponse update(Long id, TrongLuongRequest request) {
        TrongLuong entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy trọng lượng"));
        
        if (request.getGiaTriTrongLuong() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá trị trọng lượng là bắt buộc");
        }
        if (!StringUtils.hasText(request.getDonVi())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn vị là bắt buộc");
        }
        
        // Kiểm tra trùng lặp (trừ chính nó)
        if (repository.existsByGiaTriTrongLuongAndDonVi(request.getGiaTriTrongLuong(), request.getDonVi()) &&
            !(request.getGiaTriTrongLuong().equals(entity.getGiaTriTrongLuong()) && 
              request.getDonVi().equals(entity.getDonVi()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trọng lượng với giá trị và đơn vị này đã tồn tại");
        }
        
        entity.setGiaTriTrongLuong(request.getGiaTriTrongLuong());
        entity.setDonVi(request.getDonVi());
        entity.setMoTa(request.getMoTa());
        entity.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        
        return map(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy trọng lượng");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TrongLuongResponse getById(Long id) {
        return repository.findById(id)
                .map(this::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy trọng lượng"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrongLuongResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrongLuongResponse> getAllActive() {
        return repository.findByTrangThaiTrue().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private TrongLuongResponse map(TrongLuong entity) {
        TrongLuongResponse response = new TrongLuongResponse();
        response.setId(entity.getId());
        response.setGiaTriTrongLuong(entity.getGiaTriTrongLuong());
        response.setDonVi(entity.getDonVi());
        response.setMoTa(entity.getMoTa());
        response.setTrangThai(entity.getTrangThai());
        return response;
    }
}
