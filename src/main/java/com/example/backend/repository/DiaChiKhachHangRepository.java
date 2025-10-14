package com.example.backend.repository;

import com.example.backend.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Long> {
    
    List<DiaChiKhachHang> findByKhachHangId(Long khachHangId);
    
    List<DiaChiKhachHang> findByKhachHangIdAndMacDinhTrue(Long khachHangId);
    
    List<DiaChiKhachHang> findByKhachHangIdAndTrangThai(Long khachHangId, Boolean trangThai);
    
    Optional<DiaChiKhachHang> findByKhachHangIdAndMacDinhTrueAndTrangThaiTrue(Long khachHangId);
}
