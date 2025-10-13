package com.example.backend.service;

import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.dto.KhachHangDTO;
import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.entity.KhachHang;
import com.example.backend.repository.DiaChiKhachHangRepository;
import com.example.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    // Lấy tất cả khách hàng với phân trang
    public Page<KhachHangDTO> getAllKhachHang(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<KhachHang> khachHangPage = khachHangRepository.findAll(pageable);
        return khachHangPage.map(this::convertToDTO);
    }

    // Lấy khách hàng theo ID
    public Optional<KhachHangDTO> getKhachHangById(Long id) {
        return khachHangRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Tìm kiếm khách hàng với bộ lọc
    public Page<KhachHangDTO> searchKhachHang(String tenKhachHang, String email, String soDienThoai, 
                                            Boolean trangThai, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<KhachHang> khachHangPage = khachHangRepository.findWithFilters(
            tenKhachHang, email, soDienThoai, trangThai, pageable);
        
        return khachHangPage.map(this::convertToDTO);
    }

    // Tạo khách hàng mới
    public KhachHangDTO createKhachHang(KhachHangDTO khachHangDTO) {
        KhachHang khachHang = convertToEntity(khachHangDTO);
        khachHang.setNgayTao(LocalDate.now());
        khachHang.setDiemTichLuy(0);
        khachHang.setTrangThai(true);
        
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        return convertToDTO(savedKhachHang);
    }

    // Cập nhật khách hàng
    public Optional<KhachHangDTO> updateKhachHang(Long id, KhachHangDTO khachHangDTO) {
        return khachHangRepository.findById(id)
                .map(existingKhachHang -> {
                    updateEntityFromDTO(existingKhachHang, khachHangDTO);
                    KhachHang updatedKhachHang = khachHangRepository.save(existingKhachHang);
                    return convertToDTO(updatedKhachHang);
                });
    }

    // Xóa khách hàng (soft delete)
    public boolean deleteKhachHang(Long id) {
        return khachHangRepository.findById(id)
                .map(khachHang -> {
                    khachHang.setTrangThai(false);
                    khachHangRepository.save(khachHang);
                    return true;
                })
                .orElse(false);
    }

    // Xóa vĩnh viễn khách hàng
    public boolean permanentlyDeleteKhachHang(Long id) {
        if (khachHangRepository.existsById(id)) {
            khachHangRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Kiểm tra email đã tồn tại
    public boolean existsByEmail(String email) {
        return khachHangRepository.findByEmail(email).isPresent();
    }

    // Kiểm tra số điện thoại đã tồn tại
    public boolean existsBySoDienThoai(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai).isPresent();
    }

    // Lấy khách hàng theo email
    public Optional<KhachHangDTO> getKhachHangByEmail(String email) {
        return khachHangRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    // Lấy khách hàng theo số điện thoại
    public Optional<KhachHangDTO> getKhachHangBySoDienThoai(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai)
                .map(this::convertToDTO);
    }

    // Lấy khách hàng VIP (điểm tích lũy cao)
    public List<KhachHangDTO> getKhachHangVIP(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<KhachHang> vipKhachHang = khachHangRepository.findTopByDiemTichLuy(pageable);
        return vipKhachHang.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thống kê khách hàng
    public long countKhachHangByTrangThai(Boolean trangThai) {
        return khachHangRepository.countByTrangThai(trangThai);
    }

    // Lấy khách hàng theo khoảng thời gian
    public List<KhachHangDTO> getKhachHangByDateRange(LocalDate startDate, LocalDate endDate) {
        List<KhachHang> khachHangList = khachHangRepository.findByNgayTaoBetween(startDate, endDate);
        return khachHangList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Cập nhật điểm tích lũy
    public Optional<KhachHangDTO> updateDiemTichLuy(Long id, Integer diemTichLuy) {
        return khachHangRepository.findById(id)
                .map(khachHang -> {
                    khachHang.setDiemTichLuy(diemTichLuy);
                    KhachHang updatedKhachHang = khachHangRepository.save(khachHang);
                    return convertToDTO(updatedKhachHang);
                });
    }

    // Convert Entity to DTO
    private KhachHangDTO convertToDTO(KhachHang khachHang) {
        List<DiaChiKhachHangDTO> diaChiDTOs = null;
        if (khachHang.getDanhSachDiaChi() != null) {
            diaChiDTOs = khachHang.getDanhSachDiaChi().stream()
                    .map(this::convertDiaChiToDTO)
                    .collect(Collectors.toList());
        }

        return KhachHangDTO.builder()
                .id(khachHang.getId())
                .tenKhachHang(khachHang.getTenKhachHang())
                .email(khachHang.getEmail())
                .soDienThoai(khachHang.getSoDienThoai())
                .ngaySinh(khachHang.getNgaySinh())
                .gioiTinh(khachHang.getGioiTinh())
                .diemTichLuy(khachHang.getDiemTichLuy())
                .ngayTao(khachHang.getNgayTao())
                .trangThai(khachHang.getTrangThai())
                .userId(khachHang.getUser() != null ? khachHang.getUser().getId() : null)
                .username(khachHang.getUser() != null ? khachHang.getUser().getUsername() : null)
                .fullName(khachHang.getUser() != null ? khachHang.getUser().getFullName() : null)
                .danhSachDiaChi(diaChiDTOs)
                .build();
    }

    // Convert DiaChiKhachHang Entity to DTO
    private DiaChiKhachHangDTO convertDiaChiToDTO(DiaChiKhachHang diaChi) {
        return DiaChiKhachHangDTO.builder()
                .id(diaChi.getId())
                .khachHangId(diaChi.getKhachHang().getId())
                .tenNguoiNhan(diaChi.getTenNguoiNhan())
                .soDienThoai(diaChi.getSoDienThoai())
                .diaChi(diaChi.getDiaChi())
                .tinhThanh(diaChi.getTinhThanh())
                .quanHuyen(diaChi.getQuanHuyen())
                .phuongXa(diaChi.getPhuongXa())
                .macDinh(diaChi.getMacDinh())
                .trangThai(diaChi.getTrangThai())
                .build();
    }

    // Convert DTO to Entity
    private KhachHang convertToEntity(KhachHangDTO khachHangDTO) {
        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(khachHangDTO.getTenKhachHang());
        khachHang.setEmail(khachHangDTO.getEmail());
        khachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
        khachHang.setNgaySinh(khachHangDTO.getNgaySinh());
        khachHang.setGioiTinh(khachHangDTO.getGioiTinh());
        khachHang.setDiemTichLuy(khachHangDTO.getDiemTichLuy());
        khachHang.setNgayTao(khachHangDTO.getNgayTao());
        khachHang.setTrangThai(khachHangDTO.getTrangThai());
        return khachHang;
    }

    // Update Entity from DTO
    private void updateEntityFromDTO(KhachHang khachHang, KhachHangDTO khachHangDTO) {
        if (khachHangDTO.getTenKhachHang() != null) {
            khachHang.setTenKhachHang(khachHangDTO.getTenKhachHang());
        }
        if (khachHangDTO.getEmail() != null) {
            khachHang.setEmail(khachHangDTO.getEmail());
        }
        if (khachHangDTO.getSoDienThoai() != null) {
            khachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
        }
        if (khachHangDTO.getNgaySinh() != null) {
            khachHang.setNgaySinh(khachHangDTO.getNgaySinh());
        }
        if (khachHangDTO.getGioiTinh() != null) {
            khachHang.setGioiTinh(khachHangDTO.getGioiTinh());
        }
        if (khachHangDTO.getDiemTichLuy() != null) {
            khachHang.setDiemTichLuy(khachHangDTO.getDiemTichLuy());
        }
        if (khachHangDTO.getTrangThai() != null) {
            khachHang.setTrangThai(khachHangDTO.getTrangThai());
        }
    }
}
