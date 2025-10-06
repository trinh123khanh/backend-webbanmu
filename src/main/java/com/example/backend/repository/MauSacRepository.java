package com.example.backend.repository;

import com.example.backend.entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MauSacRepository extends JpaRepository<MauSac, Long> {
    boolean existsByTenMau(String tenMau);

    @Query("SELECT m FROM MauSac m WHERE (:keyword IS NULL OR LOWER(m.tenMau) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.maMau) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR m.trangThai = :trangThai)")
    Page<MauSac> search(@Param("keyword") String keyword,
                        @Param("trangThai") Boolean trangThai,
                        Pageable pageable);
}


