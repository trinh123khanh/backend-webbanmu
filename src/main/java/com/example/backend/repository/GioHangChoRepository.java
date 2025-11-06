package com.example.backend.repository;

import com.example.backend.entity.GioHangCho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangChoRepository extends JpaRepository<GioHangCho, Long> {
    
    List<GioHangCho> findByHoaDonChoId(Long hoaDonChoId);
    
    Optional<GioHangCho> findByHoaDonChoIdAndChiTietSanPhamId(Long hoaDonChoId, Long chiTietSanPhamId);
    
    void deleteByHoaDonChoId(Long hoaDonChoId);
    
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM gio_hang_cho WHERE id = :id", nativeQuery = true)
    void deleteByIdNative(@Param("id") Long id);
}

