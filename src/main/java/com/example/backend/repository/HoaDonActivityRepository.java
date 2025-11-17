package com.example.backend.repository;

import com.example.backend.entity.HoaDonActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoaDonActivityRepository extends JpaRepository<HoaDonActivity, Long> {

    /**
     * Tìm tất cả activities theo hoaDonId, sắp xếp theo thời gian giảm dần
     */
    @Query("SELECT a FROM HoaDonActivity a WHERE a.hoaDon.id = :hoaDonId ORDER BY a.performedAt DESC")
    Page<HoaDonActivity> findByHoaDonIdOrderByPerformedAtDesc(@Param("hoaDonId") Long hoaDonId, Pageable pageable);

    /**
     * Tìm tất cả activities, sắp xếp theo thời gian giảm dần
     */
    @Query("SELECT a FROM HoaDonActivity a ORDER BY a.performedAt DESC")
    Page<HoaDonActivity> findAllOrderByPerformedAtDesc(Pageable pageable);

    /**
     * Tìm activities theo mã hóa đơn
     */
    @Query("SELECT a FROM HoaDonActivity a WHERE a.maHoaDon = :maHoaDon ORDER BY a.performedAt DESC")
    Page<HoaDonActivity> findByMaHoaDonOrderByPerformedAtDesc(@Param("maHoaDon") String maHoaDon, Pageable pageable);

    /**
     * Tìm activities theo action type
     */
    @Query("SELECT a FROM HoaDonActivity a WHERE a.action = :action ORDER BY a.performedAt DESC")
    Page<HoaDonActivity> findByActionOrderByPerformedAtDesc(@Param("action") String action, Pageable pageable);

    /**
     * Tìm activities theo user
     */
    @Query("SELECT a FROM HoaDonActivity a WHERE a.user.id = :userId ORDER BY a.performedAt DESC")
    Page<HoaDonActivity> findByUserIdOrderByPerformedAtDesc(@Param("userId") Long userId, Pageable pageable);
}

