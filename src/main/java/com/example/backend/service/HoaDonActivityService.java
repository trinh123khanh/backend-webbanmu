package com.example.backend.service;

import com.example.backend.dto.HoaDonActivityDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonActivity;
import com.example.backend.entity.User;
import com.example.backend.repository.HoaDonActivityRepository;
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

@Service
public class HoaDonActivityService {

    private final HoaDonActivityRepository activityRepository;
    private final UserRepository userRepository;

    public HoaDonActivityService(HoaDonActivityRepository activityRepository,
                                 UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logActivity(HoaDon invoice, String action, String description) {
        if (invoice == null) {
            return;
        }
        logActivity(invoice.getId(), invoice.getMaHoaDon(), action, description);
    }

    @Transactional
    public void logActivity(Long hoaDonId, String maHoaDon, String action, String description) {
        ActivityActor actor = getCurrentActor();
        HoaDonActivity activity = HoaDonActivity.builder()
                .hoaDonId(hoaDonId)
                .maHoaDon(maHoaDon)
                .action(action)
                .description(description)
                .performedBy(actor.username())
                .performedByName(actor.displayName())
                .performedAt(LocalDateTime.now())
                .build();
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public Page<HoaDonActivityDTO> getActivities(Long hoaDonId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<HoaDonActivity> activityPage;
        if (hoaDonId != null) {
            activityPage = activityRepository.findByHoaDonIdOrderByPerformedAtDesc(hoaDonId, pageable);
        } else {
            activityPage = activityRepository.findAll(pageable);
        }
        return activityPage.map(this::toDTO);
    }

    private HoaDonActivityDTO toDTO(HoaDonActivity activity) {
        return HoaDonActivityDTO.builder()
                .id(activity.getId())
                .hoaDonId(activity.getHoaDonId())
                .maHoaDon(activity.getMaHoaDon())
                .action(activity.getAction())
                .description(activity.getDescription())
                .performedBy(activity.getPerformedBy())
                .performedByName(activity.getPerformedByName())
                .performedAt(activity.getPerformedAt())
                .build();
    }

    private ActivityActor getCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new ActivityActor("system", "Hệ thống");
        }
        String username = auth.getName();
        String displayName = username;
        if (StringUtils.hasText(username)) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && StringUtils.hasText(user.getFullName())) {
                displayName = user.getFullName();
            }
        }
        return new ActivityActor(username, displayName);
    }

    private record ActivityActor(String username, String displayName) {}
}

