package com.example.backend.repository;

import com.example.backend.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    boolean existsByMaSanPham(String maSanPham);

    @Query("SELECT s FROM SanPham s " +
            "WHERE (:keyword IS NULL OR LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(s.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "  AND (:trangThai IS NULL OR s.trangThai = :trangThai)")
    Page<SanPham> search(@Param("keyword") String keyword,
                         @Param("trangThai") Boolean trangThai,
                         Pageable pageable);
}


