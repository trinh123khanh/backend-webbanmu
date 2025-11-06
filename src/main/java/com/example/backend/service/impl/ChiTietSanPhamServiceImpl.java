package com.example.backend.service.impl;

import com.example.backend.dto.ChiTietSanPhamRequest;
import com.example.backend.dto.ChiTietSanPhamResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.ChiTietSanPhamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChiTietSanPhamServiceImpl implements ChiTietSanPhamService {
    private final ChiTietSanPhamRepository repository;
    private final SanPhamRepository sanPhamRepository;
    private final KichThuocRepository kichThuocRepository;
    private final MauSacRepository mauSacRepository;
    private final TrongLuongRepository trongLuongRepository;

    public ChiTietSanPhamServiceImpl(ChiTietSanPhamRepository repository,
                                    SanPhamRepository sanPhamRepository,
                                    KichThuocRepository kichThuocRepository,
                                    MauSacRepository mauSacRepository,
                                    TrongLuongRepository trongLuongRepository) {
        this.repository = repository;
        this.sanPhamRepository = sanPhamRepository;
        this.kichThuocRepository = kichThuocRepository;
        this.mauSacRepository = mauSacRepository;
        this.trongLuongRepository = trongLuongRepository;
    }

    @Override
    public ChiTietSanPhamResponse create(ChiTietSanPhamRequest request) {
        ChiTietSanPham entity = new ChiTietSanPham();
        apply(entity, request);
        return map(repository.save(entity));
    }

    @Override
    public ChiTietSanPhamResponse update(Long id, ChiTietSanPhamRequest request) {
        ChiTietSanPham entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên bản mũ"));
        apply(entity, request);
        return map(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ChiTietSanPhamResponse getById(Long id) {
        return repository.findById(id)
            .map(this::map)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên bản mũ"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietSanPhamResponse> getAll() {
        return repository.findAll().stream()
            .map(this::map)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietSanPhamResponse> getBySanPhamId(Long sanPhamId) {
        return repository.findAll().stream()
            .filter(c -> c.getSanPham() != null && c.getSanPham().getId().equals(sanPhamId))
            .map(this::map)
            .collect(Collectors.toList());
    }

    private void apply(ChiTietSanPham entity, ChiTietSanPhamRequest req) {
        entity.setSanPham(sanPhamRepository.findById(req.getSanPhamId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không hợp lệ")));
        entity.setKichThuoc(kichThuocRepository.findById(req.getKichThuocId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kích thước không hợp lệ")));
        entity.setMauSac(mauSacRepository.findById(req.getMauSacId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Màu sắc không hợp lệ")));
        // Trọng lượng: chấp nhận tên nhập tay; nếu không có, chấp nhận id; nếu cả hai trống thì để null
        if (req.getTrongLuongTen() != null && !req.getTrongLuongTen().trim().isEmpty()) {
            entity.setTrongLuongTen(req.getTrongLuongTen().trim());
        } else if (req.getTrongLuongId() != null && req.getTrongLuongId() > 0) {
            var tl = trongLuongRepository.findById(req.getTrongLuongId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trọng lượng không hợp lệ"));
            entity.setTrongLuongTen(String.valueOf(tl.getGiaTriTrongLuong()));
        } else {
            entity.setTrongLuongTen(null);
        }
        // Lưu nguyên chuỗi theo yêu cầu
        entity.setGiaBan(req.getGiaBan());
        entity.setSoLuongTon(req.getSoLuongTon());
        entity.setTrangThai(Boolean.TRUE.equals(req.getTrangThai()));
    }

    private ChiTietSanPhamResponse map(ChiTietSanPham e) {
        ChiTietSanPhamResponse r = new ChiTietSanPhamResponse();
        r.setId(e.getId());
        if (e.getSanPham() != null) {
            r.setSanPhamId(e.getSanPham().getId());
            r.setSanPhamTen(e.getSanPham().getTenSanPham());
        }
        if (e.getKichThuoc() != null) {
            r.setKichThuocId(e.getKichThuoc().getId());
            r.setKichThuocTen(e.getKichThuoc().getTenKichThuoc());
        }
        if (e.getMauSac() != null) {
            r.setMauSacId(e.getMauSac().getId());
            r.setMauSacTen(e.getMauSac().getTenMau());
            r.setMauSacMa(e.getMauSac().getMaMau());
        }
        r.setTrongLuongId(null);
        r.setTrongLuongTen(e.getTrongLuongTen());
        r.setGiaBan(e.getGiaBan());
        r.setSoLuongTon(e.getSoLuongTon());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}
