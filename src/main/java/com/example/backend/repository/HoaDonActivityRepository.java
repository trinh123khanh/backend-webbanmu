package com.example.backend.repository;

import com.example.backend.entity.HoaDonActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaDonActivityRepository extends JpaRepository<HoaDonActivity, Long> {

    Page<HoaDonActivity> findByHoaDonIdOrderByPerformedAtDesc(Long hoaDonId, Pageable pageable);

    default Page<HoaDonActivity> findAllOrderByPerformedAtDesc(Pageable pageable) {
        return findAll(pageable);
    }
}

