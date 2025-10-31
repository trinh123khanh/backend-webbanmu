package com.example.backend.repository;

import com.example.backend.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Long> {
    List<PhuongThucThanhToan> findByHoaDonId(Long hoaDonId);
}

