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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhieuGiamGiaCaNhanService {
    
    private final PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    
    /**
     * Tạo phiếu giảm giá cá nhân cho một khách hàng
     */
    @Transactional
    public PhieuGiamGiaCaNhan createPhieuGiamGiaCaNhan(Long phieuGiamGiaId, Long khachHangId) {
        try {
            log.info("Tạo phiếu giảm giá cá nhân cho phiếu ID: {} và khách hàng ID: {}", phieuGiamGiaId, khachHangId);
            
            // Validate input
            if (phieuGiamGiaId == null || phieuGiamGiaId <= 0) {
                throw new IllegalArgumentException("ID phiếu giảm giá không hợp lệ: " + phieuGiamGiaId);
            }
            if (khachHangId == null || khachHangId <= 0) {
                throw new IllegalArgumentException("ID khách hàng không hợp lệ: " + khachHangId);
            }
            
            // Kiểm tra xem khách hàng đã có phiếu giảm giá này chưa
            if (phieuGiamGiaCaNhanRepository.existsByKhachHangIdAndPhieuGiamGiaId(khachHangId, phieuGiamGiaId)) {
                log.warn("Khách hàng ID: {} đã có phiếu giảm giá ID: {}", khachHangId, phieuGiamGiaId);
                throw new RuntimeException("Khách hàng đã có phiếu giảm giá này");
            }
            
            PhieuGiamGiaCaNhan entity = PhieuGiamGiaCaNhan.builder()
                    .phieuGiamGiaId(phieuGiamGiaId)
                    .khachHangId(khachHangId)
                    .build();
            
            PhieuGiamGiaCaNhan savedEntity = phieuGiamGiaCaNhanRepository.save(entity);
            
            log.info("Tạo thành công phiếu giảm giá cá nhân ID: {}", savedEntity.getId());
            
            return savedEntity;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu giảm giá cá nhân", e);
            throw new RuntimeException("Lỗi khi tạo phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Tạo phiếu giảm giá cá nhân cho nhiều khách hàng
     */
    @Transactional
    public List<PhieuGiamGiaCaNhan> createPhieuGiamGiaCaNhanForMultipleCustomers(Long phieuGiamGiaId, List<Long> khachHangIds) {
        try {
            log.info("Tạo phiếu giảm giá cá nhân cho phiếu ID: {} và {} khách hàng: {}", 
                    phieuGiamGiaId, khachHangIds.size(), khachHangIds);
            
            List<PhieuGiamGiaCaNhan> createdEntities = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (Long khachHangId : khachHangIds) {
                try {
                    log.info("Đang tạo phiếu cá nhân cho khách hàng ID: {} với phiếu giảm giá ID: {}", 
                            khachHangId, phieuGiamGiaId);
                    
                    PhieuGiamGiaCaNhan createdEntity = createPhieuGiamGiaCaNhan(phieuGiamGiaId, khachHangId);
                    createdEntities.add(createdEntity);
                    
                    log.info("✅ Tạo thành công phiếu cá nhân ID: {} cho khách hàng ID: {}", 
                            createdEntity.getId(), khachHangId);
                    
                } catch (Exception e) {
                    String errorMsg = String.format("Lỗi khi tạo phiếu cá nhân cho khách hàng ID %d: %s", 
                            khachHangId, e.getMessage());
                    errors.add(errorMsg);
                    log.error("❌ {}", errorMsg, e);
                }
            }
            
            if (!errors.isEmpty()) {
                String allErrors = String.join("; ", errors);
                log.error("Có {} lỗi khi tạo phiếu cá nhân: {}", errors.size(), allErrors);
                throw new RuntimeException("Lỗi khi tạo phiếu cá nhân cho một số khách hàng: " + allErrors);
            }
            
            log.info("🎉 Tạo thành công {} phiếu giảm giá cá nhân cho {} khách hàng", 
                    createdEntities.size(), khachHangIds.size());
            
            return createdEntities;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu giảm giá cá nhân cho nhiều khách hàng", e);
            throw new RuntimeException("Lỗi khi tạo phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân theo phiếu giảm giá
     */
    public List<PhieuGiamGiaCaNhan> getPhieuGiamGiaCaNhanByPhieuGiamGiaId(Long phieuGiamGiaId) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo phiếu ID: {}", phieuGiamGiaId);
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findByPhieuGiamGiaIdWithDetails(phieuGiamGiaId);
            
            log.info("Lấy thành công {} phiếu giảm giá cá nhân", entities.size());
            
            return entities;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo phiếu ID: {}", phieuGiamGiaId, e);
            throw new RuntimeException("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân theo khách hàng
     */
    public List<PhieuGiamGiaCaNhan> getPhieuGiamGiaCaNhanByKhachHangId(Long khachHangId) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId);
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findByKhachHangId(khachHangId);
            
            log.info("Lấy thành công {} phiếu giảm giá cá nhân", entities.size());
            
            return entities;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId, e);
            throw new RuntimeException("Lỗi khi lấy phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Xóa tất cả phiếu giảm giá cá nhân theo phiếu giảm giá
     */
    @Transactional
    public void deletePhieuGiamGiaCaNhanByPhieuGiamGiaId(Long phieuGiamGiaId) {
        try {
            log.info("Xóa phiếu giảm giá cá nhân theo phiếu ID: {}", phieuGiamGiaId);
            
            phieuGiamGiaCaNhanRepository.deleteByPhieuGiamGiaId(phieuGiamGiaId);
            
            log.info("Xóa thành công phiếu giảm giá cá nhân theo phiếu ID: {}", phieuGiamGiaId);
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa phiếu giảm giá cá nhân theo phiếu ID: {}", phieuGiamGiaId, e);
            throw new RuntimeException("Lỗi khi xóa phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Đếm số phiếu giảm giá cá nhân theo phiếu giảm giá
     */
    public Long countPhieuGiamGiaCaNhanByPhieuGiamGiaId(Long phieuGiamGiaId) {
        try {
            return phieuGiamGiaCaNhanRepository.countByPhieuGiamGiaId(phieuGiamGiaId);
        } catch (Exception e) {
            log.error("Lỗi khi đếm phiếu giảm giá cá nhân theo phiếu ID: {}", phieuGiamGiaId, e);
            return 0L;
        }
    }
    
    /**
     * Đếm số phiếu giảm giá cá nhân theo khách hàng
     */
    public Long countPhieuGiamGiaCaNhanByKhachHangId(Long khachHangId) {
        try {
            return phieuGiamGiaCaNhanRepository.countByKhachHangId(khachHangId);
        } catch (Exception e) {
            log.error("Lỗi khi đếm phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId, e);
            return 0L;
        }
    }
    
    // ========== CÁC METHOD CHO CONTROLLER ==========
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân
     */
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> getAllPhieuGiamGiaCaNhan() {
        try {
            log.info("Lấy tất cả phiếu giảm giá cá nhân");
            
            List<PhieuGiamGiaCaNhan> entities = phieuGiamGiaCaNhanRepository.findAll();
            List<PhieuGiamGiaCaNhanResponse> responses = entities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Lấy thành công {} phiếu giảm giá cá nhân", responses.size());
            
            return ApiResponse.success("Lấy danh sách phiếu giảm giá cá nhân thành công", responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả phiếu giảm giá cá nhân", e);
            return ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tất cả phiếu giảm giá cá nhân với phân trang
     */
    public ApiResponse<Page<PhieuGiamGiaCaNhanResponse>> getAllPhieuGiamGiaCaNhanWithPagination(int page, int size) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân với phân trang - Page: {}, Size: {}", page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PhieuGiamGiaCaNhan> entityPage = phieuGiamGiaCaNhanRepository.findAll(pageable);
            
            Page<PhieuGiamGiaCaNhanResponse> responsePage = entityPage.map(this::convertToResponse);
            
            log.info("Lấy thành công {} phiếu giảm giá cá nhân với phân trang", responsePage.getContent().size());
            
            return ApiResponse.success("Lấy danh sách phiếu giảm giá cá nhân với phân trang thành công", responsePage);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân với phân trang", e);
            return ApiResponse.error("Lỗi khi lấy danh sách phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo ID
     */
    public ApiResponse<PhieuGiamGiaCaNhanResponse> getPhieuGiamGiaCaNhanById(Long id) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo ID: {}", id);
            
            PhieuGiamGiaCaNhan entity = phieuGiamGiaCaNhanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id));
            
            PhieuGiamGiaCaNhanResponse response = convertToResponse(entity);
            
            log.info("Lấy thành công phiếu giảm giá cá nhân ID: {}", id);
            
            return ApiResponse.success("Lấy phiếu giảm giá cá nhân thành công", response);
            
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
            
            log.info("Lấy thành công {} phiếu giảm giá cá nhân cho khách hàng ID: {}", responses.size(), khachHangId);
            
            return ApiResponse.success("Lấy danh sách phiếu giảm giá cá nhân theo khách hàng thành công", responses);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng ID: {}", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng: " + e.getMessage());
        }
    }
    
    /**
     * Lấy phiếu giảm giá cá nhân theo khách hàng với phân trang
     */
    public ApiResponse<Page<PhieuGiamGiaCaNhanResponse>> getPhieuGiamGiaCaNhanByKhachHangWithPagination(Long khachHangId, int page, int size) {
        try {
            log.info("Lấy phiếu giảm giá cá nhân theo khách hàng ID: {} với phân trang - Page: {}, Size: {}", khachHangId, page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<PhieuGiamGiaCaNhan> entityPage = phieuGiamGiaCaNhanRepository.findByKhachHangId(khachHangId, pageable);
            
            Page<PhieuGiamGiaCaNhanResponse> responsePage = entityPage.map(this::convertToResponse);
            
            log.info("Lấy thành công {} phiếu giảm giá cá nhân cho khách hàng ID: {} với phân trang", 
                    responsePage.getContent().size(), khachHangId);
            
            return ApiResponse.success("Lấy danh sách phiếu giảm giá cá nhân theo khách hàng với phân trang thành công", responsePage);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng ID: {} với phân trang", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy phiếu giảm giá cá nhân theo khách hàng: " + e.getMessage());
        }
    }
    
    /**
     * Tạo mới phiếu giảm giá cá nhân từ Request
     */
    public ApiResponse<PhieuGiamGiaCaNhanResponse> createPhieuGiamGiaCaNhan(PhieuGiamGiaCaNhanRequest request) {
        try {
            log.info("Tạo mới phiếu giảm giá cá nhân từ request cho khách hàng ID: {}, phiếu giảm giá ID: {}", 
                    request.getKhachHangId(), request.getPhieuGiamGiaId());
            
            // Sử dụng method có sẵn với 2 parameters
            PhieuGiamGiaCaNhan entity = createPhieuGiamGiaCaNhan(request.getPhieuGiamGiaId(), request.getKhachHangId());
            PhieuGiamGiaCaNhanResponse response = convertToResponse(entity);
            
            log.info("Tạo thành công phiếu giảm giá cá nhân ID: {}", entity.getId());
            
            return ApiResponse.success("Tạo phiếu giảm giá cá nhân thành công", response);
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu giảm giá cá nhân từ request", e);
            return ApiResponse.error("Lỗi khi tạo phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật phiếu giảm giá cá nhân
     */
    public ApiResponse<PhieuGiamGiaCaNhanResponse> updatePhieuGiamGiaCaNhan(Long id, PhieuGiamGiaCaNhanRequest request) {
        try {
            log.info("Cập nhật phiếu giảm giá cá nhân ID: {}", id);
            
            PhieuGiamGiaCaNhan entity = phieuGiamGiaCaNhanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id));
            
            // Cập nhật thông tin
            entity.setKhachHangId(request.getKhachHangId());
            entity.setPhieuGiamGiaId(request.getPhieuGiamGiaId());
            
            PhieuGiamGiaCaNhan savedEntity = phieuGiamGiaCaNhanRepository.save(entity);
            PhieuGiamGiaCaNhanResponse response = convertToResponse(savedEntity);
            
            log.info("Cập nhật thành công phiếu giảm giá cá nhân ID: {}", id);
            
            return ApiResponse.success("Cập nhật phiếu giảm giá cá nhân thành công", response);
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật phiếu giảm giá cá nhân ID: {}", id, e);
            return ApiResponse.error("Lỗi khi cập nhật phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Xóa phiếu giảm giá cá nhân theo ID
     */
    public ApiResponse<Void> deletePhieuGiamGiaCaNhan(Long id) {
        try {
            log.info("Xóa phiếu giảm giá cá nhân ID: {}", id);
            
            PhieuGiamGiaCaNhan entity = phieuGiamGiaCaNhanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá cá nhân với ID: " + id));
            
            phieuGiamGiaCaNhanRepository.delete(entity);
            
            log.info("Xóa thành công phiếu giảm giá cá nhân ID: {}", id);
            
            return ApiResponse.success("Xóa phiếu giảm giá cá nhân thành công", null);
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa phiếu giảm giá cá nhân ID: {}", id, e);
            return ApiResponse.error("Lỗi khi xóa phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Lấy thống kê phiếu giảm giá cá nhân theo khách hàng
     */
    public ApiResponse<Object> getStatisticsByKhachHang(Long khachHangId) {
        try {
            log.info("Lấy thống kê phiếu giảm giá cá nhân cho khách hàng ID: {}", khachHangId);
            
            Long totalCount = countPhieuGiamGiaCaNhanByKhachHangId(khachHangId);
            
            Map<String, Object> statistics = Map.of(
                    "khachHangId", khachHangId,
                    "totalPhieuGiamGiaCaNhan", totalCount,
                    "message", "Thống kê phiếu giảm giá cá nhân theo khách hàng"
            );
            
            log.info("Lấy thành công thống kê cho khách hàng ID: {}, tổng số phiếu: {}", khachHangId, totalCount);
            
            return ApiResponse.success("Lấy thống kê phiếu giảm giá cá nhân thành công", statistics);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê phiếu giảm giá cá nhân cho khách hàng ID: {}", khachHangId, e);
            return ApiResponse.error("Lỗi khi lấy thống kê phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Tạo phiếu giảm giá cá nhân với thông tin chi tiết và validation đầy đủ
     */
    @Transactional
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> createPersonalVouchersWithDetails(
            Long phieuGiamGiaId, List<Long> khachHangIds) {
        try {
            log.info("🚀 Bắt đầu tạo phiếu giảm giá cá nhân cho phiếu ID: {} và {} khách hàng", 
                    phieuGiamGiaId, khachHangIds.size());
            
            // Validate input
            if (phieuGiamGiaId == null || phieuGiamGiaId <= 0) {
                return ApiResponse.error("ID phiếu giảm giá không hợp lệ");
            }
            if (khachHangIds == null || khachHangIds.isEmpty()) {
                return ApiResponse.error("Danh sách khách hàng không được để trống");
            }
            
            // Tạo phiếu cá nhân cho từng khách hàng
            List<PhieuGiamGiaCaNhan> createdEntities = createPhieuGiamGiaCaNhanForMultipleCustomers(phieuGiamGiaId, khachHangIds);
            
            // Convert to response
            List<PhieuGiamGiaCaNhanResponse> responses = createdEntities.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("✅ Hoàn thành tạo {} phiếu giảm giá cá nhân", responses.size());
            
            return ApiResponse.success(
                    String.format("Tạo thành công %d phiếu giảm giá cá nhân cho %d khách hàng", 
                            responses.size(), khachHangIds.size()), 
                    responses);
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo phiếu giảm giá cá nhân với thông tin chi tiết", e);
            return ApiResponse.error("Lỗi khi tạo phiếu giảm giá cá nhân: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật toàn bộ khách hàng cho một phiếu giảm giá
     * Xóa tất cả customer cũ và thêm customer mới
     */
    @Transactional
    public ApiResponse<List<PhieuGiamGiaCaNhanResponse>> updateCustomersForPhieu(Long phieuGiamGiaId, List<Long> khachHangIds) {
        try {
            log.info("🔄 Cập nhật khách hàng cho phiếu giảm giá ID: {} với {} khách hàng mới", 
                    phieuGiamGiaId, khachHangIds != null ? khachHangIds.size() : 0);
            
            // Validate input
            if (phieuGiamGiaId == null || phieuGiamGiaId <= 0) {
                return ApiResponse.error("ID phiếu giảm giá không hợp lệ");
            }
            
            // Bước 1: Xóa tất cả khách hàng cũ
            log.info("🗑️ Xóa tất cả khách hàng cũ cho phiếu ID: {}", phieuGiamGiaId);
            deletePhieuGiamGiaCaNhanByPhieuGiamGiaId(phieuGiamGiaId);
            log.info("✅ Đã xóa tất cả khách hàng cũ");
            
            // Bước 2: Thêm khách hàng mới (nếu có)
            List<PhieuGiamGiaCaNhanResponse> responses = new ArrayList<>();
            
            if (khachHangIds != null && !khachHangIds.isEmpty()) {
                log.info("➕ Thêm {} khách hàng mới", khachHangIds.size());
                List<PhieuGiamGiaCaNhan> createdEntities = createPhieuGiamGiaCaNhanForMultipleCustomers(phieuGiamGiaId, khachHangIds);
                
                responses = createdEntities.stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList());
                
                log.info("✅ Đã thêm {} khách hàng mới thành công", responses.size());
            } else {
                log.info("ℹ️ Không có khách hàng mới để thêm");
            }
            
            log.info("🎉 Hoàn thành cập nhật khách hàng cho phiếu ID: {}", phieuGiamGiaId);
            
            return ApiResponse.success(
                    String.format("Cập nhật thành công %d khách hàng cho phiếu giảm giá", responses.size()), 
                    responses);
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi cập nhật khách hàng cho phiếu ID: {}", phieuGiamGiaId, e);
            return ApiResponse.error("Lỗi khi cập nhật khách hàng: " + e.getMessage());
        }
    }
    
    /**
     * Chuyển đổi Entity sang Response DTO
     */
    private PhieuGiamGiaCaNhanResponse convertToResponse(PhieuGiamGiaCaNhan entity) {
        return PhieuGiamGiaCaNhanResponse.builder()
                .id(entity.getId())
                .khachHangId(entity.getKhachHangId())
                .phieuGiamGiaId(entity.getPhieuGiamGiaId())
                .trangThai("Chưa sử dụng") // Default value vì Entity không có field này
                .soLanDaDung(0) // Default value vì Entity không có field này
                // Các thông tin liên quan có thể được populate từ các service khác
                .tenKhachHang("Khách hàng " + entity.getKhachHangId()) // Placeholder
                .tenPhieuGiamGia("Phiếu giảm giá " + entity.getPhieuGiamGiaId()) // Placeholder
                .maPhieuGiamGia("PGG" + entity.getPhieuGiamGiaId()) // Placeholder
                .giaTriGiam(0.0) // Placeholder
                .loaiPhieuGiamGia(false) // Placeholder
                .loaiPhieuGiamGiaText("Phần trăm") // Placeholder
                .build();
    }
}