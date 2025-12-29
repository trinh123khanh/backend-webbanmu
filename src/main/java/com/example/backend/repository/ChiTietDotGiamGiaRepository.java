package com.example.backend.repository;

import com.example.backend.entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Long> {
    List<ChiTietDotGiamGia> findByDotGiamGiaId(Long dotGiamGiaId);
    void deleteByDotGiamGiaId(Long dotGiamGiaId);
}
