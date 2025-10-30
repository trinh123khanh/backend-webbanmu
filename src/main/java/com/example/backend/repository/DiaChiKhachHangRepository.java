package com.example.backend.repository;

import com.example.backend.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Long> {
    
    @Query("SELECT d FROM DiaChiKhachHang d WHERE d.khachHang.id = :khachHangId")
    List<DiaChiKhachHang> findByKhachHangId(@Param("khachHangId") Long khachHangId);
    
    @Query("SELECT d FROM DiaChiKhachHang d WHERE d.khachHang.id = :khachHangId AND d.macDinh = true ORDER BY d.id ASC")
    List<DiaChiKhachHang> findDiaChiMacDinhByKhachHangId(@Param("khachHangId") Long khachHangId);
    
    @Query("SELECT d FROM DiaChiKhachHang d WHERE d.khachHang.id = :khachHangId AND d.trangThai = true ORDER BY d.macDinh DESC")
    List<DiaChiKhachHang> findDiaChiActiveByKhachHangId(@Param("khachHangId") Long khachHangId);
}
