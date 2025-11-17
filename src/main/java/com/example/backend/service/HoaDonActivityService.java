package com.example.backend.service;

import com.example.backend.dto.HoaDonActivityDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonActivity;
import com.example.backend.entity.User;
import com.example.backend.repository.HoaDonActivityRepository;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Service quản lý lịch sử thay đổi hóa đơn
 */
@Service
public class HoaDonActivityService {

    private final HoaDonActivityRepository activityRepository;
    private final HoaDonRepository hoaDonRepository;
    private final UserRepository userRepository;
    private final NhanVienRepository nhanVienRepository;

    public HoaDonActivityService(HoaDonActivityRepository activityRepository,
                                 HoaDonRepository hoaDonRepository,
                                 UserRepository userRepository,
                                 NhanVienRepository nhanVienRepository) {
        this.activityRepository = activityRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.userRepository = userRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Log activity với HoaDon entity
     */
    @Transactional
    public void logActivity(HoaDon invoice, String action, String description) {
        if (invoice == null) {
            return;
        }
        logActivity(invoice, action, description, null, null);
    }

    /**
     * Log activity với HoaDon entity và dữ liệu cũ/mới
     */
    @Transactional
    public void logActivity(HoaDon invoice, String action, String description, String oldData, String newData) {
        if (invoice == null) {
            return;
        }
        ActivityActor actor = getCurrentActor();
        HoaDonActivity activity = buildActivity(invoice, action, description, oldData, newData, actor);
        activityRepository.save(activity);
    }

    /**
     * Log activity với hoaDonId (fallback method)
     */
    @Transactional
    public void logActivity(Long hoaDonId, String maHoaDon, String action, String description) {
        HoaDon invoice = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (invoice == null) {
            return;
        }
        logActivity(invoice, action, description);
    }

    /**
     * Build HoaDonActivity entity từ các thông tin
     */
    private HoaDonActivity buildActivity(HoaDon invoice, String action, String description, 
                                         String oldData, String newData, ActivityActor actor) {
        HoaDonActivity.HoaDonActivityBuilder builder = HoaDonActivity.builder()
                .hoaDon(invoice)
                .maHoaDon(invoice.getMaHoaDon())
                .action(action)
                .description(description)
                .performedBy(actor.username())
                .performedByName(actor.displayName())
                .performedAt(LocalDateTime.now())
                .oldData(oldData)
                .newData(newData);

        // Set User relationship nếu có
        User user = null;
        if (StringUtils.hasText(actor.username()) && !"system".equals(actor.username())) {
            user = userRepository.findByUsername(actor.username()).orElse(null);
            if (user != null) {
                builder.user(user);
            }
        }

        // Set NhanVien relationship nếu user có liên kết với nhân viên
        if (user != null && user.getId() != null) {
            nhanVienRepository.findByUserId(user.getId()).ifPresent(builder::nhanVien);
        }

        return builder.build();
    }

    /**
     * Lấy danh sách activities với pagination
     */
    @Transactional(readOnly = true)
    public Page<HoaDonActivityDTO> getActivities(Long hoaDonId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<HoaDonActivity> activityPage;
        if (hoaDonId != null) {
            activityPage = activityRepository.findByHoaDonIdOrderByPerformedAtDesc(hoaDonId, pageable);
        } else {
            activityPage = activityRepository.findAllOrderByPerformedAtDesc(pageable);
        }
        return activityPage.map(this::toDTO);
    }

    /**
     * Convert entity to DTO
     */
    private HoaDonActivityDTO toDTO(HoaDonActivity activity) {
        Long hoaDonId = activity.getHoaDon() != null ? activity.getHoaDon().getId() : null;
        return HoaDonActivityDTO.builder()
                .id(activity.getId())
                .hoaDonId(hoaDonId)
                .maHoaDon(activity.getMaHoaDon())
                .action(activity.getAction())
                .description(activity.getDescription())
                .performedBy(activity.getPerformedBy())
                .performedByName(activity.getPerformedByName())
                .performedAt(activity.getPerformedAt())
                .oldData(activity.getOldData())
                .newData(activity.getNewData())
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())
                .build();
    }

    /**
     * Lấy thông tin người thực hiện hành động từ SecurityContext
     */
    private ActivityActor getCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new ActivityActor("system", "Hệ thống");
        }
        String username = auth.getName();
        String displayName = username;
        if (StringUtils.hasText(username)) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                // Ưu tiên lấy fullName từ User
                if (StringUtils.hasText(user.getFullName())) {
                    displayName = user.getFullName();
                }
                // Nếu user có liên kết với NhanVien, lấy tên từ NhanVien
                if (user.getId() != null) {
                    var nhanVienOpt = nhanVienRepository.findByUserId(user.getId());
                    if (nhanVienOpt.isPresent()) {
                        var nhanVien = nhanVienOpt.get();
                        if (StringUtils.hasText(nhanVien.getHoTen())) {
                            displayName = nhanVien.getHoTen();
                        }
                    }
                }
            }
        }
        return new ActivityActor(username, displayName);
    }

    private record ActivityActor(String username, String displayName) {}
}

