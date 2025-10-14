package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.PhieuGiamGia;
import com.example.backend.repository.PhieuGiamGiaRepository;
import com.example.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PhieuGiamGiaService {
    
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final KhachHangRepository khachHangRepository;
    
    // Tạo phiếu giảm giá mới
    public ApiResponse<PhieuGiamGiaResponse> createPhieuGiamGia(PhieuGiamGiaRequest request) {
        try {
            // Kiểm tra mã phiếu đã tồn tại chưa
            if (phieuGiamGiaRepository.existsByMaPhieu(request.getMaPhieu())) {
                return ApiResponse.error("Mã phiếu giảm giá đã tồn tại: " + request.getMaPhieu());
            }
            
            // Validate dữ liệu
            String validationError = validatePhieuGiamGiaRequest(request);
            if (validationError != null) {
                return ApiResponse.error(validationError);
            }
            
            // Tạo entity
            PhieuGiamGia phieuGiamGia = PhieuGiamGia.builder()
                    .maPhieu(request.getMaPhieu())
                    .tenPhieuGiamGia(request.getTenPhieuGiamGia())
                    .loaiPhieuGiamGia(request.getLoaiPhieuGiamGia())
                    .giaTriGiam(request.getGiaTriGiam())
                    .giaTriToiThieu(request.getGiaTriToiThieu())
                    .soTienToiDa(request.getSoTienToiDa())
                    .hoaDonToiThieu(request.getHoaDonToiThieu())
                    .soLuongDung(request.getSoLuongDung())
                    .ngayBatDau(request.getNgayBatDau())
                    .ngayKetThuc(request.getNgayKetThuc())
                    .trangThai(request.getTrangThai() != null ? request.getTrangThai() : true)
                    .build();
            
            // Lưu vào database
            PhieuGiamGia savedPhieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);
            
            // Convert to response
            PhieuGiamGiaResponse response = convertToResponse(savedPhieuGiamGia);
            
            log.info("Tạo phiếu giảm giá thành công: {}", savedPhieuGiamGia.getMaPhieu());
            return ApiResponse.success("Tạo phiếu giảm giá thành công", response);
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu giảm giá: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi tạo phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Cập nhật phiếu giảm giá
    public ApiResponse<PhieuGiamGiaResponse> updatePhieuGiamGia(Long id, PhieuGiamGiaRequest request) {
        try {
            Optional<PhieuGiamGia> existingPhieuGiamGia = phieuGiamGiaRepository.findById(id);
            if (existingPhieuGiamGia.isEmpty()) {
                return ApiResponse.error("Không tìm thấy phiếu giảm giá với ID: " + id);
            }
            
            PhieuGiamGia phieuGiamGia = existingPhieuGiamGia.get();
            
            // Kiểm tra mã phiếu có thay đổi không và có trùng không
            if (!phieuGiamGia.getMaPhieu().equals(request.getMaPhieu()) && 
                phieuGiamGiaRepository.existsByMaPhieu(request.getMaPhieu())) {
                return ApiResponse.error("Mã phiếu giảm giá đã tồn tại: " + request.getMaPhieu());
            }
            
            // Validate dữ liệu
            String validationError = validatePhieuGiamGiaRequest(request);
            if (validationError != null) {
                return ApiResponse.error(validationError);
            }
            
            // Cập nhật thông tin
            phieuGiamGia.setMaPhieu(request.getMaPhieu());
            phieuGiamGia.setTenPhieuGiamGia(request.getTenPhieuGiamGia());
            phieuGiamGia.setLoaiPhieuGiamGia(request.getLoaiPhieuGiamGia());
            phieuGiamGia.setGiaTriGiam(request.getGiaTriGiam());
            phieuGiamGia.setGiaTriToiThieu(request.getGiaTriToiThieu());
            phieuGiamGia.setSoTienToiDa(request.getSoTienToiDa());
            phieuGiamGia.setHoaDonToiThieu(request.getHoaDonToiThieu());
            phieuGiamGia.setSoLuongDung(request.getSoLuongDung());
            phieuGiamGia.setNgayBatDau(request.getNgayBatDau());
            phieuGiamGia.setNgayKetThuc(request.getNgayKetThuc());
            phieuGiamGia.setTrangThai(request.getTrangThai());
            
            // Lưu vào database
            PhieuGiamGia updatedPhieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);
            
            // Convert to response
            PhieuGiamGiaResponse response = convertToResponse(updatedPhieuGiamGia);
            
            log.info("Cập nhật phiếu giảm giá thành công: {}", updatedPhieuGiamGia.getMaPhieu());
            return ApiResponse.success("Cập nhật phiếu giảm giá thành công", response);
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật phiếu giảm giá: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi cập nhật phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Xóa phiếu giảm giá
    public ApiResponse<String> deletePhieuGiamGia(Long id) {
        try {
            Optional<PhieuGiamGia> phieuGiamGia = phieuGiamGiaRepository.findById(id);
            if (phieuGiamGia.isEmpty()) {
                return ApiResponse.error("Không tìm thấy phiếu giảm giá với ID: " + id);
            }
            
            phieuGiamGiaRepository.deleteById(id);
            
            log.info("Xóa phiếu giảm giá thành công: {}", phieuGiamGia.get().getMaPhieu());
            return ApiResponse.success("Xóa phiếu giảm giá thành công");
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa phiếu giảm giá: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi xóa phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Lấy phiếu giảm giá theo ID
    @Transactional(readOnly = true)
    public ApiResponse<PhieuGiamGiaResponse> getPhieuGiamGiaById(Long id) {
        try {
            Optional<PhieuGiamGia> phieuGiamGia = phieuGiamGiaRepository.findById(id);
            if (phieuGiamGia.isEmpty()) {
                return ApiResponse.error("Không tìm thấy phiếu giảm giá với ID: " + id);
            }
            
            PhieuGiamGiaResponse response = convertToResponse(phieuGiamGia.get());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá theo ID: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Lấy phiếu giảm giá theo mã phiếu
    @Transactional(readOnly = true)
    public ApiResponse<PhieuGiamGiaResponse> getPhieuGiamGiaByMaPhieu(String maPhieu) {
        try {
            Optional<PhieuGiamGia> phieuGiamGia = phieuGiamGiaRepository.findByMaPhieu(maPhieu);
            if (phieuGiamGia.isEmpty()) {
                return ApiResponse.error("Không tìm thấy phiếu giảm giá với mã: " + maPhieu);
            }
            
            PhieuGiamGiaResponse response = convertToResponse(phieuGiamGia.get());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá theo mã: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Lấy danh sách phiếu giảm giá với phân trang
    @Transactional(readOnly = true)
    public ApiResponse<PhieuGiamGiaListResponse> getAllPhieuGiamGia(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<PhieuGiamGia> phieuGiamGiaPage = phieuGiamGiaRepository.findAll(pageable);
            
            List<PhieuGiamGiaResponse> responses = phieuGiamGiaPage.getContent()
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            PhieuGiamGiaListResponse listResponse = PhieuGiamGiaListResponse.builder()
                    .data(responses)
                    .total(phieuGiamGiaPage.getTotalElements())
                    .page(page)
                    .size(size)
                    .totalPages(phieuGiamGiaPage.getTotalPages())
                    .hasNext(phieuGiamGiaPage.hasNext())
                    .hasPrevious(phieuGiamGiaPage.hasPrevious())
                    .build();
            
            return ApiResponse.success(listResponse);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phiếu giảm giá: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Lấy phiếu giảm giá đang hoạt động
    @Transactional(readOnly = true)
    public ApiResponse<List<PhieuGiamGiaResponse>> getActivePhieuGiamGia() {
        try {
            List<PhieuGiamGia> activeVouchers = phieuGiamGiaRepository.findActiveVouchers(LocalDate.now());
            List<PhieuGiamGiaResponse> responses = activeVouchers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá đang hoạt động: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá đang hoạt động: " + e.getMessage());
        }
    }
    
    // Tìm kiếm phiếu giảm giá
    @Transactional(readOnly = true)
    public ApiResponse<List<PhieuGiamGiaResponse>> searchPhieuGiamGia(String keyword) {
        try {
            List<PhieuGiamGia> searchResults = phieuGiamGiaRepository.searchByKeyword(keyword);
            List<PhieuGiamGiaResponse> responses = searchResults.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm phiếu giảm giá: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi tìm kiếm phiếu giảm giá: " + e.getMessage());
        }
    }
    
    // Đếm số lượng phiếu giảm giá đang hoạt động
    @Transactional(readOnly = true)
    public ApiResponse<Long> getActiveVoucherCount() {
        try {
            long count = phieuGiamGiaRepository.countActiveVouchers(LocalDate.now());
            return ApiResponse.success(count);
            
        } catch (Exception e) {
            log.error("Lỗi khi đếm phiếu giảm giá đang hoạt động: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi đếm phiếu giảm giá đang hoạt động: " + e.getMessage());
        }
    }
    
    // Lấy phiếu giảm giá sắp hết hạn
    @Transactional(readOnly = true)
    public ApiResponse<List<PhieuGiamGiaResponse>> getExpiringSoonVouchers(int days) {
        try {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().plusDays(days);
            
            List<PhieuGiamGia> expiringVouchers = phieuGiamGiaRepository.findExpiringSoon(startDate, endDate);
            List<PhieuGiamGiaResponse> responses = expiringVouchers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá sắp hết hạn: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá sắp hết hạn: " + e.getMessage());
        }
    }
    
    // Chuyển đổi entity sang response DTO
    private PhieuGiamGiaResponse convertToResponse(PhieuGiamGia phieuGiamGia) {
        return PhieuGiamGiaResponse.builder()
                .id(phieuGiamGia.getId())
                .maPhieu(phieuGiamGia.getMaPhieu())
                .tenPhieuGiamGia(phieuGiamGia.getTenPhieuGiamGia())
                .loaiPhieuGiamGia(phieuGiamGia.getLoaiPhieuGiamGia())
                .loaiPhieuGiamGiaText(phieuGiamGia.getLoaiPhieuGiamGiaText())
                .giaTriGiam(phieuGiamGia.getGiaTriGiam())
                .giaTriToiThieu(phieuGiamGia.getGiaTriToiThieu())
                .soTienToiDa(phieuGiamGia.getSoTienToiDa())
                .hoaDonToiThieu(phieuGiamGia.getHoaDonToiThieu())
                .soLuongDung(phieuGiamGia.getSoLuongDung())
                .ngayBatDau(phieuGiamGia.getNgayBatDau())
                .ngayKetThuc(phieuGiamGia.getNgayKetThuc())
                .trangThai(phieuGiamGia.getTrangThai())
                .trangThaiText(phieuGiamGia.getTrangThai() ? "Hoạt động" : "Không hoạt động")
                .isActive(phieuGiamGia.isActive())
                .isExpired(phieuGiamGia.isExpired())
                .isNotStarted(phieuGiamGia.isNotStarted())
                .build();
    }
    
    // Validate dữ liệu request
    private String validatePhieuGiamGiaRequest(PhieuGiamGiaRequest request) {
        if (request.getMaPhieu() == null || request.getMaPhieu().trim().isEmpty()) {
            return "Mã phiếu giảm giá không được để trống";
        }
        
        if (request.getTenPhieuGiamGia() == null || request.getTenPhieuGiamGia().trim().isEmpty()) {
            return "Tên phiếu giảm giá không được để trống";
        }
        
        if (request.getLoaiPhieuGiamGia() == null) {
            return "Loại phiếu giảm giá không được để trống";
        }
        
        if (request.getGiaTriGiam() == null || request.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            return "Giá trị giảm phải lớn hơn 0";
        }
        
        if (request.getGiaTriToiThieu() == null || request.getGiaTriToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            return "Giá trị tối thiểu không được âm";
        }
        
        if (request.getSoTienToiDa() == null || request.getSoTienToiDa().compareTo(BigDecimal.ZERO) < 0) {
            return "Số tiền tối đa không được âm";
        }
        
        if (request.getHoaDonToiThieu() == null || request.getHoaDonToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            return "Hóa đơn tối thiểu không được âm";
        }
        
        if (request.getSoLuongDung() == null || request.getSoLuongDung() <= 0) {
            return "Số lượng dùng phải lớn hơn 0";
        }
        
        if (request.getNgayBatDau() == null) {
            return "Ngày bắt đầu không được để trống";
        }
        
        if (request.getNgayKetThuc() == null) {
            return "Ngày kết thúc không được để trống";
        }
        
        if (request.getNgayBatDau().isAfter(request.getNgayKetThuc())) {
            return "Ngày bắt đầu phải trước ngày kết thúc";
        }
        
        return null; // Không có lỗi
    }
    
    // Lấy danh sách khách hàng cho form phiếu giảm giá
    public ApiResponse<java.util.List<com.example.backend.dto.KhachHangDTO>> getAllCustomersForVoucher() {
        try {
            log.info("Lấy danh sách khách hàng cho form phiếu giảm giá");
            
            // Lấy tất cả khách hàng từ database
            List<com.example.backend.entity.KhachHang> khachHangList = khachHangRepository.findAll();
            
            // Chuyển đổi sang DTO
            List<com.example.backend.dto.KhachHangDTO> khachHangDTOList = khachHangList.stream()
                    .map(this::convertKhachHangToDTO)
                    .collect(Collectors.toList());
            
            log.info("Lấy được {} khách hàng", khachHangDTOList.size());
            return ApiResponse.success(khachHangDTOList);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách khách hàng: {}", e.getMessage(), e);
            return ApiResponse.error("Không thể lấy danh sách khách hàng: " + e.getMessage());
        }
    }
    
    // Chuyển đổi KhachHang entity sang DTO
    private com.example.backend.dto.KhachHangDTO convertKhachHangToDTO(com.example.backend.entity.KhachHang khachHang) {
        return com.example.backend.dto.KhachHangDTO.builder()
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
                .build();
    }

    /**
     * Toggle trạng thái của phiếu giảm giá
     */
    public ApiResponse<PhieuGiamGiaResponse> togglePhieuGiamGiaStatus(Long id) {
        try {
            log.info("Toggle trạng thái phiếu giảm giá ID: {}", id);
            
            // Tìm phiếu giảm giá theo ID
            Optional<PhieuGiamGia> phieuGiamGiaOpt = phieuGiamGiaRepository.findById(id);
            
            if (phieuGiamGiaOpt.isEmpty()) {
                log.warn("Không tìm thấy phiếu giảm giá với ID: {}", id);
                return ApiResponse.error("Không tìm thấy phiếu giảm giá với ID: " + id);
            }
            
            PhieuGiamGia phieuGiamGia = phieuGiamGiaOpt.get();
            
            // Toggle trạng thái
            phieuGiamGia.setTrangThai(!phieuGiamGia.getTrangThai());
            
            // Lưu vào database
            PhieuGiamGia savedPhieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);
            
            // Chuyển đổi sang response
            PhieuGiamGiaResponse response = convertToResponse(savedPhieuGiamGia);
            
            log.info("Toggle trạng thái thành công cho phiếu giảm giá ID: {}, trạng thái mới: {}", 
                    id, savedPhieuGiamGia.getTrangThai());
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi toggle trạng thái phiếu giảm giá ID: {}", id, e);
            return ApiResponse.error("Lỗi khi cập nhật trạng thái phiếu giảm giá: " + e.getMessage());
        }
    }
}
