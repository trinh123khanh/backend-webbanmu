package com.example.backend.service;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PhieuGiamGiaCaNhanRequest;
import com.example.backend.dto.PhieuGiamGiaCaNhanResponse;
import com.example.backend.entity.PhieuGiamGiaCaNhan;
import com.example.backend.repository.PhieuGiamGiaCaNhanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhieuGiamGiaCaNhanService {
    
    private final PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân
     */
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> getAllPhieuGiamGiaCaNhan() {
        try {
            log.info("Lấy danh sách phiếu giảm giá cá nhân");
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findAllWithPhieuGiamGia();
            
            List<PhieuGiamGiaCaNhanResponse> responses = entities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Lấy danh sách thành công: {} phiếu giảm giá cá nhân", responses.size());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phiếu giảm giá cá nhân", e);
            return ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân với phân trang
     */
    public ApiResponse<Page<PhieuGiamGiaCaNhanResponse>> getAllPhieuGiamGiaCaNhanWithPagination(int page, int size) {
        try {
            log.info("Lấy danh sách phiếu giảm giá cá nhân với phân trang - Page: {}, Size: {}", page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findAllWithPhieuGiamGia(pageable);
            
            Page<PhieuGiamGiaCaNhanResponse> responses = entities.map(this::convertToResponse);
            
            log.info("Lấy danh sách thành công: {} phiếu giảm giá cá nhân", responses.getTotalElements());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phiếu giảm giá cá nhân với phân trang", e);
            return ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo ID
     */
    public ApiResponse<PhieuGiamGiaCaNhanResponse> getPhieuGiamGiaCaNhanById(Long id) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo ID: {}", id);
            
            Optional<PhieuGiamGiaCaNhan> entityOpt = phieuGiamGiaCaNhanRepository.findByIdWithPhieuGiamGia(id);
            
            if (entityOpt.isEmpty()) {
                log.warn("Không tìm thấy phiếu giảm giá cá nhân với ID: {}", id);
                return ApiResponse.error("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id);
            }
            
            PhieuGiamGiaCaNhanResponse response = convertToResponse(entityOpt.get());
            
            log.info("Lấy thành công phiếu giảm giá cá nhân ID: {}", id);
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo ID: {}", id, e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo khách hàng
     */
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> getPhieuGiamGiaCaNhanByKhachHang(Long khachHangId) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId);
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findByKhachHangId(khachHangId);
            
            List<PhieuGiamGiaCaNhanResponse> responses = entities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Lấy thành công: {} phiếu giảm giá cá nhân cho khách hàng ID: {}", responses.size(), khachHangId);
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo khách hàng với phân trang
     */
    public ApiResponse<Page<PhieuGiamGiaCaNhanResponse>> getPhieuGiamGiaCaNhanByKhachHangWithPagination(Long khachHangId, int page, int size) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo khách hàng ID: {} với phân trang - Page: {}, Size: {}", khachHangId, page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findByKhachHangId(khachHangId, pageable);
            
            Page<PhieuGiamGiaCaNhanResponse> responses = entities.map(this::convertToResponse);
            
            log.info("Lấy thành công: {} phiếu giảm giá cá nhân cho khách hàng ID: {}", responses.getTotalElements(), khachHangId);
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng ID: {} với phân trang", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân có thể sử dụng
     */
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> getAvailableVouchers() {
        try {
            log.info("Lấy danh sách phiếu giảm giá cá nhân có thể sử dụng");
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findAvailableVouchers();
            
            List<PhieuGiamGiaCaNhanResponse> responses = entities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Lấy thành công: {} phiếu giảm giá cá nhân có thể sử dụng", responses.size());
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân có thể sử dụng", e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân có thể sử dụng của khách hàng
     */
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> getAvailableVouchersByKhachHang(Long khachHangId) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân có thể sử dụng của khách hàng ID: {}", khachHangId);
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findAvailableVouchersByKhachHang(khachHangId);
            
            List<PhieuGiamGiaCaNhanResponse> responses = entities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Lấy thành công: {} phiếu giảm giá cá nhân có thể sử dụng cho khách hàng ID: {}", responses.size(), khachHangId);
            
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân có thể sử dụng của khách hàng ID: {}", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Tạo mới phiếu giảm giá cá nhân
     */
    @Transactional
    public ApiResponse<PhieuGiamGiaCaNhanResponse> createPhieuGiamGiaCaNhan(PhieuGiamGiaCaNhanRequest request) {
        try {
            log.info("Tạo mới phiếu giảm giá cá nhân cho khách hàng ID: {}, phiếu giảm giá ID: {}", 
                    request.getKhachHangId(), request.getPhieuGiamGiaId());
            
            // Kiểm tra xem khách hàng đã có phiếu giảm giá này chưa
            if (phieuGiamGiaCaNhanRepository.existsByKhachHangIdAndPhieuGiamGiaId(
                    request.getKhachHangId(), request.getPhieuGiamGiaId())) {
                log.warn("Khách hàng ID: {} đã có phiếu giảm giá ID: {}", 
                        request.getKhachHangId(), request.getPhieuGiamGiaId());
                return ApiResponse.error("Khách hàng đã có phiếu giảm giá này");
            }
            
            PhieuGiamGiaCaNhan entity = PhieuGiamGiaCaNhan.builder()
                    .khachHangId(request.getKhachHangId())
                    .phieuGiamGiaId(request.getPhieuGiamGiaId())
                    .daSuDung(request.getDaSuDung() != null ? request.getDaSuDung() : false)
                    .ngayHetHan(request.getNgayHetHan())
                    .ngaySuDung(request.getNgaySuDung())
                    .build();
            
            PhieuGiamGiaCaNhan savedEntity = phieuGiamGiaCaNhanRepository.save(entity);
            
            // Lấy lại với thông tin liên quan
            Optional<PhieuGiamGiaCaNhan> entityWithDetails = phieuGiamGiaCaNhanRepository.findByIdWithPhieuGiamGia(savedEntity.getId());
            
            if (entityWithDetails.isEmpty()) {
                return ApiResponse.error("Lỗi khi tạo phiếu giảm giá cá nhân");
            }
            
            PhieuGiamGiaCaNhanResponse response = convertToResponse(entityWithDetails.get());
            
            log.info("Tạo thành công phiếu giảm giá cá nhân ID: {}", savedEntity.getId());
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu giảm giá cá nhân", e);
            return ApiResponse.error("Lỗi khi tạo phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật phiếu giảm giá cá nhân
     */
    @Transactional
    public ApiResponse<PhieuGiamGiaCaNhanResponse> updatePhieuGiamGiaCaNhan(Long id, PhieuGiamGiaCaNhanRequest request) {
        try {
            log.info("Cập nhật phiếu giảm giá cá nhân ID: {}", id);
            
            Optional<PhieuGiamGiaCaNhan> entityOpt = phieuGiamGiaCaNhanRepository.findById(id);
            
            if (entityOpt.isEmpty()) {
                log.warn("Không tìm thấy phiếu giảm giá cá nhân với ID: {}", id);
                return ApiResponse.error("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id);
            }
            
            PhieuGiamGiaCaNhan entity = entityOpt.get();
            
            // Cập nhật các trường
            if (request.getKhachHangId() != null) {
                entity.setKhachHangId(request.getKhachHangId());
            }
            if (request.getPhieuGiamGiaId() != null) {
                entity.setPhieuGiamGiaId(request.getPhieuGiamGiaId());
            }
            if (request.getDaSuDung() != null) {
                entity.setDaSuDung(request.getDaSuDung());
            }
            if (request.getNgayHetHan() != null) {
                entity.setNgayHetHan(request.getNgayHetHan());
            }
            if (request.getNgaySuDung() != null) {
                entity.setNgaySuDung(request.getNgaySuDung());
            }
            
            PhieuGiamGiaCaNhan savedEntity = phieuGiamGiaCaNhanRepository.save(entity);
            
            // Lấy lại với thông tin liên quan
            Optional<PhieuGiamGiaCaNhan> entityWithDetails = phieuGiamGiaCaNhanRepository.findByIdWithPhieuGiamGia(savedEntity.getId());
            
            if (entityWithDetails.isEmpty()) {
                return ApiResponse.error("Lỗi khi cập nhật phiếu giảm giá cá nhân");
            }
            
            PhieuGiamGiaCaNhanResponse response = convertToResponse(entityWithDetails.get());
            
            log.info("Cập nhật thành công phiếu giảm giá cá nhân ID: {}", id);
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật phiếu giảm giá cá nhân ID: {}", id, e);
            return ApiResponse.error("Lỗi khi cập nhật phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Xóa phiếu giảm giá cá nhân
     */
    @Transactional
    public ApiResponse<Void> deletePhieuGiamGiaCaNhan(Long id) {
        try {
            log.info("Xóa phiếu giảm giá cá nhân ID: {}", id);
            
            if (!phieuGiamGiaCaNhanRepository.existsById(id)) {
                log.warn("Không tìm thấy phiếu giảm giá cá nhân với ID: {}", id);
                return ApiResponse.error("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id);
            }
            
            phieuGiamGiaCaNhanRepository.deleteById(id);
            
            log.info("Xóa thành công phiếu giảm giá cá nhân ID: {}", id);
            
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa phiếu giảm giá cá nhân ID: {}", id, e);
            return ApiResponse.error("Lỗi khi xóa phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Đánh dấu phiếu giảm giá cá nhân đã sử dụng
     */
    @Transactional
    public ApiResponse<PhieuGiamGiaCaNhanResponse> markAsUsed(Long id) {
        try {
            log.info("Đánh dấu phiếu giảm giá cá nhân ID: {} đã sử dụng", id);
            
            Optional<PhieuGiamGiaCaNhan> entityOpt = phieuGiamGiaCaNhanRepository.findById(id);
            
            if (entityOpt.isEmpty()) {
                log.warn("Không tìm thấy phiếu giảm giá cá nhân với ID: {}", id);
                return ApiResponse.error("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id);
            }
            
            PhieuGiamGiaCaNhan entity = entityOpt.get();
            
            if (entity.getDaSuDung()) {
                log.warn("Phiếu giảm giá cá nhân ID: {} đã được sử dụng", id);
                return ApiResponse.error("Phiếu giảm giá cá nhân đã được sử dụng");
            }
            
            if (entity.isExpired()) {
                log.warn("Phiếu giảm giá cá nhân ID: {} đã hết hạn", id);
                return ApiResponse.error("Phiếu giảm giá cá nhân đã hết hạn");
            }
            
            entity.markAsUsed();
            PhieuGiamGiaCaNhan savedEntity = phieuGiamGiaCaNhanRepository.save(entity);
            
            // Lấy lại với thông tin liên quan
            Optional<PhieuGiamGiaCaNhan> entityWithDetails = phieuGiamGiaCaNhanRepository.findByIdWithPhieuGiamGia(savedEntity.getId());
            
            if (entityWithDetails.isEmpty()) {
                return ApiResponse.error("Lỗi khi đánh dấu phiếu giảm giá cá nhân đã sử dụng");
            }
            
            PhieuGiamGiaCaNhanResponse response = convertToResponse(entityWithDetails.get());
            
            log.info("Đánh dấu thành công phiếu giảm giá cá nhân ID: {} đã sử dụng", id);
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu phiếu giảm giá cá nhân ID: {} đã sử dụng", id, e);
            return ApiResponse.error("Lỗi khi đánh dấu phiếu giảm giá cá nhân đã sử dụng: " + e.getMessage());
        }
    }
    
    /**
     * Lấy thống kê phiếu giảm giá cá nhân theo khách hàng
     */
    public ApiResponse<Object> getStatisticsByKhachHang(Long khachHangId) {
        try {
            log.info("Lấy thống kê phiếu giảm giá cá nhân cho khách hàng ID: {}", khachHangId);
            
            Long totalCount = phieuGiamGiaCaNhanRepository.countByKhachHangId(khachHangId);
            Long usedCount = phieuGiamGiaCaNhanRepository.countUsedVouchersByKhachHang(khachHangId);
            Long availableCount = phieuGiamGiaCaNhanRepository.countAvailableVouchersByKhachHang(khachHangId);
            
            Object statistics = new Object() {
                public final Long totalVouchers = totalCount;
                public final Long usedVouchers = usedCount;
                public final Long availableVouchers = availableCount;
                public final Long expiredVouchers = totalCount - usedCount - availableCount;
            };
            
            log.info("Lấy thành công thống kê cho khách hàng ID: {} - Tổng: {}, Đã dùng: {}, Có thể dùng: {}", 
                    khachHangId, totalCount, usedCount, availableCount);
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê phiếu giảm giá cá nhân cho khách hàng ID: {}", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy thống kê phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Chuyển đổi entity sang response
     */
    private PhieuGiamGiaCaNhanResponse convertToResponse(PhieuGiamGiaCaNhan entity) {
        return PhieuGiamGiaCaNhanResponse.builder()
                .id(entity.getId())
                .khachHangId(entity.getKhachHangId())
                .phieuGiamGiaId(entity.getPhieuGiamGiaId())
                .daSuDung(entity.getDaSuDung())
                .ngayHetHan(entity.getNgayHetHan())
                .ngaySuDung(entity.getNgaySuDung())
                .trangThai(entity.getTrangThaiText())
                .soLanDaDung(entity.getDaSuDung() ? 1 : 0)
                .tenKhachHang("Khách hàng " + entity.getKhachHangId()) // Tạm thời, sẽ cần join với bảng khách hàng
                .tenPhieuGiamGia(entity.getPhieuGiamGia() != null ? entity.getPhieuGiamGia().getTenPhieuGiamGia() : "N/A")
                .maPhieuGiamGia(entity.getPhieuGiamGia() != null ? entity.getPhieuGiamGia().getMaPhieu() : "N/A")
                .giaTriGiam(entity.getPhieuGiamGia() != null ? entity.getPhieuGiamGia().getGiaTriGiam().doubleValue() : 0.0)
                .loaiPhieuGiamGia(entity.getPhieuGiamGia() != null ? entity.getPhieuGiamGia().getLoaiPhieuGiamGia() : false)
                .loaiPhieuGiamGiaText(entity.getPhieuGiamGia() != null ? 
                    (entity.getPhieuGiamGia().getLoaiPhieuGiamGia() ? "Tiền mặt" : "Phần trăm") : "N/A")
                .build();
    }
}
