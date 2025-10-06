package com.example.backend.repository;

import com.example.backend.entity.NhaSanXuat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NhaSanXuatRepository extends JpaRepository<NhaSanXuat, Long> {
    boolean existsByTenNhaSanXuat(String tenNhaSanXuat);

    @Query("SELECT n FROM NhaSanXuat n WHERE (:keyword IS NULL OR LOWER(n.tenNhaSanXuat) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR n.trangThai = :trangThai)")
    Page<NhaSanXuat> search(@Param("keyword") String keyword,
                            @Param("trangThai") Boolean trangThai,
                            Pageable pageable);
}



