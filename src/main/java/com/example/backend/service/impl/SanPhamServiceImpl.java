package com.example.backend.service.impl;

import com.example.backend.dto.SanPhamRequest;
import com.example.backend.dto.SanPhamResponse;
import com.example.backend.entity.SanPham;
import com.example.backend.repository.ChatLieuVoRepository;
import com.example.backend.repository.CongNgheAnToanRepository;
import com.example.backend.repository.LoaiMuBaoHiemRepository;
import com.example.backend.repository.NhaSanXuatRepository;
import com.example.backend.repository.KieuDangMuRepository;
import com.example.backend.repository.SanPhamRepository;
import com.example.backend.repository.TrongLuongRepository;
import com.example.backend.repository.XuatXuRepository;
import com.example.backend.service.SanPhamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@Transactional
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final LoaiMuBaoHiemRepository loaiMuBaoHiemRepository;
    private final NhaSanXuatRepository nhaSanXuatRepository;
    private final ChatLieuVoRepository chatLieuVoRepository;
    private final TrongLuongRepository trongLuongRepository;
    private final XuatXuRepository xuatXuRepository;
    private final KieuDangMuRepository kieuDangMuRepository;
    private final CongNgheAnToanRepository congNgheAnToanRepository;

    public SanPhamServiceImpl(SanPhamRepository sanPhamRepository,
                              LoaiMuBaoHiemRepository loaiMuBaoHiemRepository,
                              NhaSanXuatRepository nhaSanXuatRepository,
                              ChatLieuVoRepository chatLieuVoRepository,
                              TrongLuongRepository trongLuongRepository,
                              XuatXuRepository xuatXuRepository,
                              KieuDangMuRepository kieuDangMuRepository,
                              CongNgheAnToanRepository congNgheAnToanRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.loaiMuBaoHiemRepository = loaiMuBaoHiemRepository;
        this.nhaSanXuatRepository = nhaSanXuatRepository;
        this.chatLieuVoRepository = chatLieuVoRepository;
        this.trongLuongRepository = trongLuongRepository;
        this.xuatXuRepository = xuatXuRepository;
        this.kieuDangMuRepository = kieuDangMuRepository;
        this.congNgheAnToanRepository = congNgheAnToanRepository;
    }

    @Override
    public SanPhamResponse create(SanPhamRequest request) {
        validateRequest(request);
        if (sanPhamRepository.existsByMaSanPham(request.getMaSanPham())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã sản phẩm đã tồn tại");
        }
        SanPham entity = new SanPham();
        apply(entity, request);
        entity.setNgayTao(LocalDate.now());
        return map(sanPhamRepository.save(entity));
    }

    @Override
    public SanPhamResponse update(Long id, SanPhamRequest request) {
        validateRequest(request);
        SanPham entity = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        if (!request.getMaSanPham().equals(entity.getMaSanPham()) && sanPhamRepository.existsByMaSanPham(request.getMaSanPham())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã sản phẩm đã tồn tại");
        }
        apply(entity, request);
        return map(sanPhamRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        sanPhamRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SanPhamResponse getById(Long id) {
        return sanPhamRepository.findById(id).map(this::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return sanPhamRepository.search(keyword, trangThai, pageable).map(this::map);
    }

    private void validateRequest(SanPhamRequest request) {
        if (!StringUtils.hasText(request.getMaSanPham()) || !StringUtils.hasText(request.getTenSanPham())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã và Tên sản phẩm là bắt buộc");
        }
        if (request.getGiaBan() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá bán là bắt buộc");
        }
    }

    private void apply(SanPham e, SanPhamRequest r) {
        e.setMaSanPham(r.getMaSanPham());
        e.setTenSanPham(r.getTenSanPham());
        e.setMoTa(r.getMoTa());
        e.setLoaiMuBaoHiem(loaiMuBaoHiemRepository.findById(r.getLoaiMuBaoHiemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại mũ bảo hiểm không hợp lệ")));
        e.setNhaSanXuat(nhaSanXuatRepository.findById(r.getNhaSanXuatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nhà sản xuất không hợp lệ")));
        e.setChatLieuVo(chatLieuVoRepository.findById(r.getChatLieuVoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chất liệu vỏ không hợp lệ")));
        e.setTrongLuong(trongLuongRepository.findById(r.getTrongLuongId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trọng lượng không hợp lệ")));
        e.setXuatXu(xuatXuRepository.findById(r.getXuatXuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Xuất xứ không hợp lệ")));
        e.setKieuDangMu(kieuDangMuRepository.findById(r.getKieuDangMuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kiểu dáng mũ không hợp lệ")));
        e.setCongNgheAnToan(congNgheAnToanRepository.findById(r.getCongNgheAnToanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Công nghệ an toàn không hợp lệ")));
        e.setGiaBan(r.getGiaBan());
        e.setTrangThai(Boolean.TRUE.equals(r.getTrangThai()));
    }

    private SanPhamResponse map(SanPham e) {
        SanPhamResponse r = new SanPhamResponse();
        r.setId(e.getId());
        r.setMaSanPham(e.getMaSanPham());
        r.setTenSanPham(e.getTenSanPham());
        r.setMoTa(e.getMoTa());
        if (e.getLoaiMuBaoHiem() != null) { r.setLoaiMuBaoHiemId(e.getLoaiMuBaoHiem().getId()); r.setLoaiMuBaoHiemTen(e.getLoaiMuBaoHiem().getTenLoai()); }
        if (e.getNhaSanXuat() != null) { r.setNhaSanXuatId(e.getNhaSanXuat().getId()); r.setNhaSanXuatTen(e.getNhaSanXuat().getTenNhaSanXuat()); }
        if (e.getChatLieuVo() != null) { r.setChatLieuVoId(e.getChatLieuVo().getId()); r.setChatLieuVoTen(e.getChatLieuVo().getTenChatLieu()); }
        if (e.getTrongLuong() != null) { r.setTrongLuongId(e.getTrongLuong().getId()); r.setTrongLuongTen(String.valueOf(e.getTrongLuong().getGiaTriTrongLuong())); }
        if (e.getXuatXu() != null) { r.setXuatXuId(e.getXuatXu().getId()); r.setXuatXuTen(e.getXuatXu().getTenXuatXu()); }
        if (e.getKieuDangMu() != null) { r.setKieuDangMuId(e.getKieuDangMu().getId()); r.setKieuDangMuTen(e.getKieuDangMu().getTenKieuDang()); }
        if (e.getCongNgheAnToan() != null) { r.setCongNgheAnToanId(e.getCongNgheAnToan().getId()); r.setCongNgheAnToanTen(e.getCongNgheAnToan().getTenCongNghe()); }
        r.setGiaBan(e.getGiaBan());
        r.setNgayTao(e.getNgayTao());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}


