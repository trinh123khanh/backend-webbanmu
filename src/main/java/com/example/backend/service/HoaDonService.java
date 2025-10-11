package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.KhachHang;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;

    // Convert Entity to DTO
    @Transactional(readOnly = true)
    private HoaDonDTO convertToDTO(HoaDon hoaDon) {
        HoaDonDTO.HoaDonDTOBuilder builder = HoaDonDTO.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .khachHangId(hoaDon.getKhachHangId())
                .nhanVienId(hoaDon.getNhanVienId())
                .ngayTao(hoaDon.getNgayTao())
                .ngayThanhToan(hoaDon.getNgayThanhToan())
                .tongTien(hoaDon.getTongTien())
                .tienGiamGia(hoaDon.getTienGiamGia())
                .thanhTien(hoaDon.getThanhTien())
                .ghiChu(hoaDon.getGhiChu())
                .trangThai(hoaDon.getTrangThai());
        
        // Populate customer information if khachHangId exists
        if (hoaDon.getKhachHangId() != null) {
            System.out.println("Looking for customer with ID: " + hoaDon.getKhachHangId());
            Optional<KhachHang> khachHang = khachHangRepository.findById(hoaDon.getKhachHangId());
            if (khachHang.isPresent()) {
                KhachHang customer = khachHang.get();
                System.out.println("Found customer: " + customer.getTenKhachHang());
                builder.tenKhachHang(customer.getTenKhachHang())
                       .emailKhachHang(customer.getEmail())
                       .soDienThoaiKhachHang(customer.getSoDienThoai());
            } else {
                System.out.println("Customer not found with ID: " + hoaDon.getKhachHangId());
            }
        } else {
            System.out.println("No khachHangId found in invoice");
        }
        
        return builder.build();
    }

    // Convert DTO to Entity
    private HoaDon convertToEntity(HoaDonDTO hoaDonDTO) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(hoaDonDTO.getId());
        hoaDon.setMaHoaDon(hoaDonDTO.getMaHoaDon());
        hoaDon.setKhachHangId(hoaDonDTO.getKhachHangId());
        hoaDon.setNhanVienId(hoaDonDTO.getNhanVienId());
        hoaDon.setNgayTao(hoaDonDTO.getNgayTao() != null ? hoaDonDTO.getNgayTao() : LocalDateTime.now());
        hoaDon.setNgayThanhToan(hoaDonDTO.getNgayThanhToan());
        hoaDon.setTongTien(hoaDonDTO.getTongTien());
        hoaDon.setTienGiamGia(hoaDonDTO.getTienGiamGia());
        hoaDon.setThanhTien(hoaDonDTO.getThanhTien());
        hoaDon.setGhiChu(hoaDonDTO.getGhiChu());
        hoaDon.setTrangThai(hoaDonDTO.getTrangThai());
        return hoaDon;
    }

    public List<HoaDonDTO> getAllHoaDon() {
        return hoaDonRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Page<HoaDonDTO> getHoaDonPaginated(String search, String trangThai, int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        HoaDon.TrangThaiHoaDon trangThaiEnum = null;
        if (trangThai != null && !trangThai.equalsIgnoreCase("all")) {
            try {
                trangThaiEnum = HoaDon.TrangThaiHoaDon.valueOf(trangThai.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Handle invalid status string
            }
        }

        Page<HoaDon> hoaDonPage = hoaDonRepository.searchAndFilter(search, trangThaiEnum, pageable);
        List<HoaDonDTO> dtos = hoaDonPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, hoaDonPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Optional<HoaDonDTO> getHoaDonById(Long id) {
        return hoaDonRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional
    public HoaDonDTO createHoaDon(HoaDonDTO hoaDonDTO) {
        hoaDonDTO.setNgayTao(LocalDateTime.now());
        HoaDon hoaDon = convertToEntity(hoaDonDTO);
        return convertToDTO(hoaDonRepository.save(hoaDon));
    }

    @Transactional
    public Optional<HoaDonDTO> updateHoaDon(Long id, HoaDonDTO hoaDonDTO) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    HoaDon updatedHoaDon = convertToEntity(hoaDonDTO);
                    updatedHoaDon.setId(existingHoaDon.getId()); // Ensure the ID is not changed
                    return convertToDTO(hoaDonRepository.save(updatedHoaDon));
                });
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        hoaDonRepository.deleteById(id);
    }
}
