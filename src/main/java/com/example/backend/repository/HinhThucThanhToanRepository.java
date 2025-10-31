package com.example.backend.repository;

import com.example.backend.entity.HinhThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HinhThucThanhToanRepository extends JpaRepository<HinhThucThanhToan, Long> {
    Optional<HinhThucThanhToan> findByTenHinhThuc(String tenHinhThuc);
}

