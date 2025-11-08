package com.example.backend.repository;

import com.example.backend.entity.ThongTinDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThongTinDonHangRepository extends JpaRepository<ThongTinDonHang, Long> {
    Optional<ThongTinDonHang> findByHoaDonId(Long hoaDonId);
}

