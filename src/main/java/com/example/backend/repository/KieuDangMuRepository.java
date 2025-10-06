package com.example.backend.repository;

import com.example.backend.entity.KieuDangMu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KieuDangMuRepository extends JpaRepository<KieuDangMu, Long> {
    boolean existsByTenKieuDang(String tenKieuDang);

    @Query("SELECT k FROM KieuDangMu k WHERE (:keyword IS NULL OR LOWER(k.tenKieuDang) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR k.trangThai = :trangThai)")
    Page<KieuDangMu> search(@Param("keyword") String keyword,
                             @Param("trangThai") Boolean trangThai,
                             Pageable pageable);
}


