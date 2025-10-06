package com.example.backend.repository;

import com.example.backend.entity.KichThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KichThuocRepository extends JpaRepository<KichThuoc, Long> {
    boolean existsByTenKichThuoc(String tenKichThuoc);

    @Query("SELECT k FROM KichThuoc k WHERE (:keyword IS NULL OR LOWER(k.tenKichThuoc) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR k.trangThai = :trangThai)")
    Page<KichThuoc> search(@Param("keyword") String keyword,
                           @Param("trangThai") Boolean trangThai,
                           Pageable pageable);
}


