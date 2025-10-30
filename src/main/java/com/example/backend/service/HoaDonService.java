package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.dto.HoaDonChiTietDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonChiTiet;
import com.example.backend.repository.HoaDonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public HoaDonService(HoaDonRepository hoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    public HoaDonDTO toDTO(HoaDon h) {
        HoaDonDTO.HoaDonDTOBuilder builder = HoaDonDTO.builder()
                .id(h.getId())
                .maHoaDon(h.getMaHoaDon())
                .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                .ngayTao(h.getNgayTao())
                .ngayThanhToan(h.getNgayThanhToan())
                .tongTien(h.getTongTien())
                .tienGiamGia(h.getTienGiamGia())
                .giamGiaPhanTram(h.getGiamGiaPhanTram())
                .thanhTien(h.getThanhTien())
                .ghiChu(h.getGhiChu())
                .trangThai(h.getTrangThai())
                .soLuongSanPham(h.getSoLuongSanPham());
        
        // Map thông tin khách hàng
        if (h.getKhachHang() != null) {
            builder.tenKhachHang(h.getKhachHang().getTenKhachHang())
                   .emailKhachHang(h.getKhachHang().getEmail())
                   .soDienThoaiKhachHang(h.getKhachHang().getSoDienThoai())
                   .diaChiKhachHang(h.getKhachHang().getDiaChi());
        }
        
        // Map thông tin nhân viên
        if (h.getNhanVien() != null) {
            builder.tenNhanVien(h.getNhanVien().getHoTen());
        }
        
        // Map danh sách chi tiết sản phẩm
        if (h.getDanhSachChiTiet() != null && !h.getDanhSachChiTiet().isEmpty()) {
            List<HoaDonChiTietDTO> chiTietList = h.getDanhSachChiTiet().stream()
                    .map(this::toChiTietDTO)
                    .collect(Collectors.toList());
            builder.danhSachChiTiet(chiTietList);
        }
        
        return builder.build();
    }
    
    private HoaDonChiTietDTO toChiTietDTO(HoaDonChiTiet ct) {
        HoaDonChiTietDTO.HoaDonChiTietDTOBuilder builder = HoaDonChiTietDTO.builder()
                .id(ct.getId())
                .chiTietSanPhamId(ct.getChiTietSanPham() != null ? ct.getChiTietSanPham().getId() : null)
                .soLuong(ct.getSoLuong())
                .donGia(ct.getDonGia())
                .giamGia(ct.getGiamGia())
                .thanhTien(ct.getThanhTien());
        
        // Map thông tin sản phẩm từ chi_tiet_san_pham
        if (ct.getChiTietSanPham() != null) {
            var chiTietSP = ct.getChiTietSanPham();
            
            // Lấy thông tin từ san_pham
            if (chiTietSP.getSanPham() != null) {
                var sanPham = chiTietSP.getSanPham();
                builder.tenSanPham(sanPham.getTenSanPham())
                       .maSanPham(sanPham.getMaSanPham())
                       .anhSanPham(sanPham.getAnhSanPham());
                
                // Lấy nhà sản xuất
                if (sanPham.getNhaSanXuat() != null) {
                    builder.nhaSanXuat(sanPham.getNhaSanXuat().getTenNhaSanXuat());
                }
            }
            
            // Lấy màu sắc
            if (chiTietSP.getMauSac() != null) {
                builder.mauSac(chiTietSP.getMauSac().getTenMau());
            }
            
            // Lấy kích thước
            if (chiTietSP.getKichThuoc() != null) {
                builder.kichThuoc(chiTietSP.getKichThuoc().getTenKichThuoc());
            }
        }
        
        return builder.build();
    }

    private void updateEntityFromDTO(HoaDon h, HoaDonDTO d) {
        if (d.getMaHoaDon() != null) h.setMaHoaDon(d.getMaHoaDon());
        if (d.getNgayThanhToan() != null) h.setNgayThanhToan(d.getNgayThanhToan());
        if (d.getTongTien() != null) h.setTongTien(d.getTongTien());
        if (d.getTienGiamGia() != null) h.setTienGiamGia(d.getTienGiamGia());
        if (d.getGiamGiaPhanTram() != null) h.setGiamGiaPhanTram(d.getGiamGiaPhanTram());
        if (d.getThanhTien() != null) h.setThanhTien(d.getThanhTien());
        if (d.getGhiChu() != null) h.setGhiChu(d.getGhiChu());
        if (d.getTrangThai() != null) h.setTrangThai(d.getTrangThai());
        if (d.getSoLuongSanPham() != null) h.setSoLuongSanPham(d.getSoLuongSanPham());
    }

    @Transactional
    public HoaDonDTO createHoaDon(HoaDonDTO dto) {
        HoaDon h = new HoaDon();
        h.setMaHoaDon(dto.getMaHoaDon());
        h.setNgayTao(LocalDateTime.now());
        updateEntityFromDTO(h, dto);
        HoaDon saved = hoaDonRepository.save(h);
        return toDTO(saved);
    }

    @Transactional
    public HoaDonDTO updateHoaDon(Long id, HoaDonDTO dto) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        updateEntityFromDTO(h, dto);
        HoaDon saved = hoaDonRepository.save(h);
        return toDTO(saved);
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        hoaDonRepository.delete(h);
    }

    public Page<HoaDon> getAllHoaDon(String keyword, Pageable pageable) {
        // Đếm tổng số bản ghi trước
        jakarta.persistence.TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(DISTINCT h) FROM HoaDon h " +
            "WHERE (:keyword IS NULL OR h.maHoaDon LIKE :keyword)",
            Long.class
        );
        
        if (keyword != null && !keyword.isEmpty()) {
            countQuery.setParameter("keyword", "%" + keyword + "%");
        } else {
            countQuery.setParameter("keyword", null);
        }
        long totalElements = countQuery.getSingleResult();
        
        // Query với join fetch để load các relationships
        jakarta.persistence.TypedQuery<HoaDon> query = entityManager.createQuery(
            "SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.nhanVien " +
            "LEFT JOIN FETCH h.danhSachChiTiet c " +
            "LEFT JOIN FETCH c.chiTietSanPham ct " +
            "LEFT JOIN FETCH ct.sanPham s " +
            "LEFT JOIN FETCH s.nhaSanXuat " +
            "LEFT JOIN FETCH ct.mauSac " +
            "LEFT JOIN FETCH ct.kichThuoc " +
            "WHERE (:keyword IS NULL OR h.maHoaDon LIKE :keyword) " +
            "ORDER BY h.ngayTao DESC",
            HoaDon.class
        );
        
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        } else {
            query.setParameter("keyword", null);
        }
        
        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<HoaDon> results = query.getResultList();
        
        // Create a Page manually
        return new org.springframework.data.domain.PageImpl<>(results, pageable, totalElements);
    }

    public Optional<HoaDon> getHoaDonById(Long id) {
        jakarta.persistence.TypedQuery<HoaDon> query = entityManager.createQuery(
            "SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.nhanVien " +
            "LEFT JOIN FETCH h.danhSachChiTiet c " +
            "LEFT JOIN FETCH c.chiTietSanPham ct " +
            "LEFT JOIN FETCH ct.sanPham s " +
            "LEFT JOIN FETCH s.nhaSanXuat " +
            "LEFT JOIN FETCH ct.mauSac " +
            "LEFT JOIN FETCH ct.kichThuoc " +
            "WHERE h.id = :id",
            HoaDon.class
        );
        query.setParameter("id", id);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }
}
