package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.PhieuGiamGia;
import com.example.backend.entity.PhieuGiamGiaCaNhan;
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
import java.util.ArrayList;
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
    private final PhieuGiamGiaCaNhanService phieuGiamGiaCaNhanService;
    private final EmailService emailService;
    
    // Tạo phiếu giảm giá mới với 2 chế độ: Công khai và Cá nhân
    public ApiResponse<PhieuGiamGiaResponse> createPhieuGiamGia(PhieuGiamGiaRequest request) {
        try {
            log.info("Tạo phiếu giảm giá mới với chế độ: {}", request.getIsPublic() ? "Công khai" : "Cá nhân");
            
            // Kiểm tra mã phiếu đã tồn tại chưa
            if (phieuGiamGiaRepository.existsByMaPhieu(request.getMaPhieu())) {
                return ApiResponse.error("Mã phiếu giảm giá đã tồn tại: " + request.getMaPhieu());
            }
            
            // Validate dữ liệu
            String validationError = validatePhieuGiamGiaRequest(request);
            if (validationError != null) {
                return ApiResponse.error(validationError);
            }
            
            // Validate cho chế độ Cá nhân
            if (!request.getIsPublic() && (request.getSelectedCustomerIds() == null || request.getSelectedCustomerIds().isEmpty())) {
                return ApiResponse.error("Chế độ Cá nhân yêu cầu phải chọn ít nhất một khách hàng");
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
            
            // Lưu vào database và lấy ID
            PhieuGiamGia savedPhieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);
            Long phieuGiamGiaId = savedPhieuGiamGia.getId();
            
            log.info("Tạo phiếu giảm giá thành công với ID: {}", phieuGiamGiaId);
            
            // Xử lý chế độ Cá nhân
            if (!request.getIsPublic() && request.getSelectedCustomerIds() != null && !request.getSelectedCustomerIds().isEmpty()) {
                try {
                    log.info("Tạo phiếu giảm giá cá nhân cho {} khách hàng: {}", 
                            request.getSelectedCustomerIds().size(), request.getSelectedCustomerIds());
                    
                    // Validate khách hàng tồn tại
                    List<Long> invalidCustomerIds = validateCustomerIds(request.getSelectedCustomerIds());
                    if (!invalidCustomerIds.isEmpty()) {
                        log.error("Không tìm thấy khách hàng với ID: {}", invalidCustomerIds);
                        throw new RuntimeException("Không tìm thấy khách hàng với ID: " + invalidCustomerIds);
                    }
                    
                    // Tạo phiếu giảm giá cá nhân cho từng khách hàng
                    List<PhieuGiamGiaCaNhan> createdPersonalVouchers = 
                            phieuGiamGiaCaNhanService.createPhieuGiamGiaCaNhanForMultipleCustomers(
                                    phieuGiamGiaId, 
                                    request.getSelectedCustomerIds()
                            );
                    
                    log.info("Tạo thành công {} phiếu giảm giá cá nhân cho {} khách hàng", 
                            createdPersonalVouchers.size(), request.getSelectedCustomerIds().size());
                    
                    // Log chi tiết từng phiếu đã tạo
                    for (PhieuGiamGiaCaNhan personalVoucher : createdPersonalVouchers) {
                        log.info("Đã tạo phiếu cá nhân ID: {} cho khách hàng ID: {} với phiếu giảm giá ID: {}", 
                                personalVoucher.getId(), personalVoucher.getKhachHangId(), personalVoucher.getPhieuGiamGiaId());
                    }
                    
                    // Gửi email thông báo cho các khách hàng đã chọn
                    try {
                        log.info("Bắt đầu gửi email thông báo cho {} khách hàng", request.getSelectedCustomerIds().size());
                        
                        for (Long customerId : request.getSelectedCustomerIds()) {
                            // Lấy thông tin khách hàng từ database
                            Optional<com.example.backend.entity.KhachHang> khachHangOpt = khachHangRepository.findById(customerId);
                            
                            if (khachHangOpt.isPresent()) {
                                com.example.backend.entity.KhachHang khachHang = khachHangOpt.get();
                                
                                // Chỉ gửi email nếu khách hàng có email
                                if (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty()) {
                                    emailService.sendPhieuGiamGiaNotification(
                                            khachHang.getEmail(),
                                            khachHang.getTenKhachHang(),
                                            savedPhieuGiamGia.getMaPhieu(),
                                            savedPhieuGiamGia.getTenPhieuGiamGia()
                                    );
                                    log.info("Đã gửi email thông báo tới khách hàng {} ({})", khachHang.getTenKhachHang(), khachHang.getEmail());
                                } else {
                                    log.warn("Khách hàng ID: {} không có email, bỏ qua gửi email", customerId);
                                }
                            } else {
                                log.warn("Không tìm thấy khách hàng với ID: {}, bỏ qua gửi email", customerId);
                            }
                        }
                        
                        log.info("Hoàn thành gửi email thông báo");
                        
                    } catch (Exception emailException) {
                        // Không throw exception để không ảnh hưởng đến việc tạo phiếu giảm giá
                        log.error("Lỗi khi gửi email thông báo, nhưng phiếu giảm giá đã được tạo thành công: {}", emailException.getMessage());
                    }
                    
                } catch (Exception e) {
                    log.error("Lỗi khi tạo phiếu giảm giá cá nhân, sẽ rollback toàn bộ transaction", e);
                    throw new RuntimeException("Lỗi khi tạo phiếu giảm giá cá nhân: " + e.getMessage());
                }
            }
            
            // Convert to response
            PhieuGiamGiaResponse response = convertToResponse(savedPhieuGiamGia);
            
            String successMessage = request.getIsPublic() ? 
                    "Tạo phiếu giảm giá công khai thành công" : 
                    "Tạo phiếu giảm giá cá nhân thành công cho " + request.getSelectedCustomerIds().size() + " khách hàng";
            
            return ApiResponse.success(successMessage, response);
            
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
    
    // Lấy phiếu giảm giá đang hoạt động (đang diễn ra)
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
    
    // Lấy phiếu giảm giá theo trạng thái động (sắp diễn ra, đang diễn ra, kết thúc)
    @Transactional(readOnly = true)
    public ApiResponse<List<PhieuGiamGiaResponse>> getPhieuGiamGiaByDynamicStatus(String status) {
        try {
            LocalDate today = LocalDate.now();
            List<PhieuGiamGia> vouchers = new ArrayList<>();
            
            switch (status.toLowerCase()) {
                case "sap_dien_ra":
                    // Phiếu chưa bắt đầu (ngày bắt đầu > hôm nay)
                    vouchers = phieuGiamGiaRepository.findByNgayBatDauAfter(today);
                    break;
                case "dang_dien_ra":
                    // Phiếu đang diễn ra (ngày bắt đầu <= hôm nay <= ngày kết thúc)
                    vouchers = phieuGiamGiaRepository.findActiveVouchers(today);
                    break;
                case "ket_thuc":
                    // Phiếu đã kết thúc (ngày kết thúc < hôm nay)
                    vouchers = phieuGiamGiaRepository.findByNgayKetThucBefore(today);
                    break;
                default:
                    return ApiResponse.error("Trạng thái không hợp lệ. Chỉ chấp nhận: sap_dien_ra, dang_dien_ra, ket_thuc");
            }
            
            List<PhieuGiamGiaResponse> responses = vouchers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Lấy {} phiếu giảm giá với trạng thái: {}", responses.size(), status);
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá theo trạng thái {}: {}", status, e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá theo trạng thái: " + e.getMessage());
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
        try {
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
        } catch (Exception e) {
            log.error("Lỗi khi convert PhieuGiamGia sang Response: {}", e.getMessage(), e);
            // Trả về response cơ bản nếu có lỗi
            return PhieuGiamGiaResponse.builder()
                    .id(phieuGiamGia.getId())
                    .maPhieu(phieuGiamGia.getMaPhieu())
                    .tenPhieuGiamGia(phieuGiamGia.getTenPhieuGiamGia())
                    .loaiPhieuGiamGia(phieuGiamGia.getLoaiPhieuGiamGia())
                    .loaiPhieuGiamGiaText(phieuGiamGia.getLoaiPhieuGiamGia() ? "Tiền mặt" : "Phần trăm")
                    .giaTriGiam(phieuGiamGia.getGiaTriGiam())
                    .giaTriToiThieu(phieuGiamGia.getGiaTriToiThieu())
                    .soTienToiDa(phieuGiamGia.getSoTienToiDa())
                    .hoaDonToiThieu(phieuGiamGia.getHoaDonToiThieu())
                    .soLuongDung(phieuGiamGia.getSoLuongDung())
                    .ngayBatDau(phieuGiamGia.getNgayBatDau())
                    .ngayKetThuc(phieuGiamGia.getNgayKetThuc())
                    .trangThai(phieuGiamGia.getTrangThai())
                    .trangThaiText(phieuGiamGia.getTrangThai() ? "Hoạt động" : "Không hoạt động")
                    .isActive(false)
                    .isExpired(false)
                    .isNotStarted(false)
                    .build();
        }
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
                .maKhachHang(khachHang.getMaKhachHang())
                .tenKhachHang(khachHang.getTenKhachHang())
                .email(khachHang.getEmail())
                .soDienThoai(khachHang.getSoDienThoai())
                .ngaySinh(khachHang.getNgaySinh())
                .gioiTinh(khachHang.getGioiTinh())
                .diemTichLuy(khachHang.getDiemTichLuy())
                .ngayTao(khachHang.getNgayTao())
                .trangThai(khachHang.getTrangThai())
                .soLanMua(khachHang.getSoLanMua())
                .lanMuaGanNhat(khachHang.getLanMuaGanNhat())
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
    
    /**
     * Tính toán trạng thái động của phiếu giảm giá dựa trên thời gian thực tế
     */
    public String calculateDynamicStatus(PhieuGiamGia phieuGiamGia) {
        LocalDate today = LocalDate.now();
        
        if (phieuGiamGia.getNgayBatDau().isAfter(today)) {
            return "sap_dien_ra";
        } else if (phieuGiamGia.getNgayKetThuc().isBefore(today)) {
            return "ket_thuc";
        } else {
            return "dang_dien_ra";
        }
    }
    
    /**
     * Lấy phiếu giảm giá với trạng thái động được tính toán
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<PhieuGiamGiaResponse>> getAllPhieuGiamGiaWithDynamicStatus() {
        try {
            List<PhieuGiamGia> allVouchers = phieuGiamGiaRepository.findAll();
            
            List<PhieuGiamGiaResponse> responses = allVouchers.stream()
                    .map(voucher -> {
                        PhieuGiamGiaResponse response = convertToResponse(voucher);
                        // Thêm trạng thái động vào response
                        response.setTrangThaiText(calculateDynamicStatus(voucher));
                        return response;
                    })
                    .collect(Collectors.toList());
            
            log.info("Lấy {} phiếu giảm giá với trạng thái động", responses.size());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá với trạng thái động: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá: " + e.getMessage());
        }
    }
    
    /**
     * Validate danh sách ID khách hàng có tồn tại trong database không
     */
    private List<Long> validateCustomerIds(List<Long> customerIds) {
        try {
            log.info("Validate {} khách hàng với ID: {}", customerIds.size(), customerIds);
            
            List<Long> invalidIds = new ArrayList<>();
            
            for (Long customerId : customerIds) {
                if (customerId == null || customerId <= 0) {
                    invalidIds.add(customerId);
                    log.warn("ID khách hàng không hợp lệ: {}", customerId);
                    continue;
                }
                
                // Kiểm tra khách hàng có tồn tại không
                boolean exists = khachHangRepository.existsById(customerId);
                if (!exists) {
                    invalidIds.add(customerId);
                    log.warn("Không tìm thấy khách hàng với ID: {}", customerId);
                } else {
                    log.debug("Khách hàng ID: {} tồn tại", customerId);
                }
            }
            
            log.info("Validate hoàn thành: {} khách hàng hợp lệ, {} khách hàng không hợp lệ", 
                    customerIds.size() - invalidIds.size(), invalidIds.size());
            
            return invalidIds;
            
        } catch (Exception e) {
            log.error("Lỗi khi validate danh sách khách hàng", e);
            throw new RuntimeException("Lỗi khi validate danh sách khách hàng: " + e.getMessage());
        }
    }
}
