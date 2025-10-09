package com.example.backend.repository;

import com.example.backend.entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface XuatXuRepository extends JpaRepository<XuatXu, Long> {
    boolean existsByTenXuatXu(String tenXuatXu);

    @Query("SELECT x FROM XuatXu x WHERE (:keyword IS NULL OR LOWER(x.tenXuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(COALESCE(x.moTa, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR x.trangThai = :trangThai)")
    Page<XuatXu> search(@Param("keyword") String keyword,
                        @Param("trangThai") Boolean trangThai,
                        Pageable pageable);
}


