package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.dto.SanPhamTrongHoaDon;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final ThongTinDonHangRepository thongTinDonHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository,
                         HoaDonChiTietRepository hoaDonChiTietRepository,
                         ChiTietSanPhamRepository chiTietSanPhamRepository,
                         SanPhamRepository sanPhamRepository,
                         ThongTinDonHangRepository thongTinDonHangRepository,
                         KhachHangRepository khachHangRepository,
                         NhanVienRepository nhanVienRepository,
                         DiaChiKhachHangRepository diaChiKhachHangRepository,
                         PhuongThucThanhToanRepository phuongThucThanhToanRepository,
                         HinhThucThanhToanRepository hinhThucThanhToanRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.thongTinDonHangRepository = thongTinDonHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.hinhThucThanhToanRepository = hinhThucThanhToanRepository;
    }

    @Transactional
    public HoaDon createHoaDon(HoaDonDTO dto) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa(dto.getMa());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setTrangThai(dto.getTrangThai());

        // Khách hàng
        if (dto.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(dto.getKhachHangId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));
            hoaDon.setKhachHang(khachHang);
        }

        // Nhân viên
        if (dto.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(dto.getNhanVienId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhân viên"));
            hoaDon.setNhanVien(nhanVien);
        }

        // Lưu hóa đơn trước
        hoaDonRepository.save(hoaDon);

        // Địa chỉ khách hàng
        if (dto.getDiaChiId() != null) {
            DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(dto.getDiaChiId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy địa chỉ khách hàng"));
            ThongTinDonHang thongTin = new ThongTinDonHang();
            thongTin.setHoaDon(hoaDon);
            thongTin.setDiaChiKhachHang(diaChi);
            thongTinDonHangRepository.save(thongTin);
        }

        // Phương thức thanh toán
        if (dto.getPhuongThucThanhToanId() != null) {
            PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(dto.getPhuongThucThanhToanId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phương thức thanh toán"));
            pttt.setHoaDon(hoaDon);
            phuongThucThanhToanRepository.save(pttt);
        }

        // Thêm chi tiết sản phẩm
        if (dto.getSanPhamList() != null && !dto.getSanPhamList().isEmpty()) {
            for (SanPhamTrongHoaDon sp : dto.getSanPhamList()) {
                ChiTietSanPham chiTiet = chiTietSanPhamRepository.findById(sp.getChiTietSanPhamId())
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chi tiết sản phẩm"));
                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHoaDon(hoaDon);
                hoaDonChiTiet.setChiTietSanPham(chiTiet);
                hoaDonChiTiet.setSoLuong(sp.getSoLuong());
                hoaDonChiTiet.setDonGia(sp.getDonGia());
                hoaDonChiTietRepository.save(hoaDonChiTiet);
            }
        }

        return hoaDon;
    }

    @Transactional
    public HoaDon updateHoaDon(UUID id, HoaDonDTO dto) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));

        hoaDon.setTrangThai(dto.getTrangThai());
        hoaDon.setNgayCapNhat(LocalDateTime.now());

        // Cập nhật khách hàng
        if (dto.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(dto.getKhachHangId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));
            hoaDon.setKhachHang(khachHang);
        }

        // Cập nhật phương thức thanh toán
        if (dto.getPhuongThucThanhToanId() != null) {
            PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(dto.getPhuongThucThanhToanId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phương thức thanh toán"));
            pttt.setHoaDon(hoaDon);
            phuongThucThanhToanRepository.save(pttt);
        }

        // Cập nhật chi tiết hóa đơn
        if (dto.getSanPhamList() != null) {
            hoaDonChiTietRepository.deleteByHoaDonId(hoaDon.getId());
            for (SanPhamTrongHoaDon sp : dto.getSanPhamList()) {
                ChiTietSanPham chiTiet = chiTietSanPhamRepository.findById(sp.getChiTietSanPhamId())
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chi tiết sản phẩm"));
                HoaDonChiTiet hdct = new HoaDonChiTiet();
                hdct.setHoaDon(hoaDon);
                hdct.setChiTietSanPham(chiTiet);
                hdct.setSoLuong(sp.getSoLuong());
                hdct.setDonGia(sp.getDonGia());
                hoaDonChiTietRepository.save(hdct);
            }
        }

        return hoaDonRepository.save(hoaDon);
    }

    @Transactional
    public void deleteHoaDon(UUID id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        thongTinDonHangRepository.deleteByHoaDonId(hoaDon.getId());
        hoaDonChiTietRepository.deleteByHoaDonId(hoaDon.getId());
        phuongThucThanhToanRepository.deleteByHoaDonId(hoaDon.getId());
        hoaDonRepository.delete(hoaDon);
    }

    public Page<HoaDon> getAllHoaDon(String keyword, Pageable pageable) {
        Specification<HoaDon> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(root.get("ma"), "%" + keyword + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return hoaDonRepository.findAll(spec, pageable);
    }

    public Optional<HoaDon> getHoaDonById(UUID id) {
        return hoaDonRepository.findById(id);
    }
}
