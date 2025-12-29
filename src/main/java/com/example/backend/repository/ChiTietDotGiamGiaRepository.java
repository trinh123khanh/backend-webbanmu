package com.example.backend.repository;

import com.example.backend.entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Long> {
    List<ChiTietDotGiamGia> findByDotGiamGiaId(Long dotGiamGiaId);
    void deleteByDotGiamGiaId(Long dotGiamGiaId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ChiTietDotGiamGia c WHERE c.chiTietSanPham.id = :id AND c.trangThai = true AND c.dotGiamGia.trangThai = true AND c.dotGiamGia.ngayBatDau <= CURRENT_TIMESTAMP AND c.dotGiamGia.ngayKetThuc >= CURRENT_TIMESTAMP ORDER BY c.id DESC")
    java.util.List<ChiTietDotGiamGia> findActivePromotionForProduct(@org.springframework.data.repository.query.Param("id") Long id);
}
