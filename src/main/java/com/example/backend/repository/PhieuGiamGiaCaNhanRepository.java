package com.example.backend.repository;

import com.example.backend.entity.PhieuGiamGiaCaNhan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Long> {
    
    // Lấy tất cả phiếu giảm giá cá nhân theo phiếu giảm giá
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGiaId(Long phieuGiamGiaId);
    
    // Lấy tất cả phiếu giảm giá cá nhân theo khách hàng
    List<PhieuGiamGiaCaNhan> findByKhachHangId(Long khachHangId);
    
    // Kiểm tra xem khách hàng đã có phiếu giảm giá này chưa
    boolean existsByKhachHangIdAndPhieuGiamGiaId(Long khachHangId, Long phieuGiamGiaId);
    
    // Xóa tất cả phiếu giảm giá cá nhân theo phiếu giảm giá
    void deleteByPhieuGiamGiaId(Long phieuGiamGiaId);
    
    // Đếm số phiếu giảm giá cá nhân theo phiếu giảm giá
    Long countByPhieuGiamGiaId(Long phieuGiamGiaId);
    
    // Đếm số phiếu giảm giá cá nhân theo khách hàng
    Long countByKhachHangId(Long khachHangId);
    
    // Lấy phiếu giảm giá cá nhân với thông tin liên quan
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p LEFT JOIN FETCH p.phieuGiamGia WHERE p.phieuGiamGiaId = :phieuGiamGiaId")
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGiaIdWithDetails(@Param("phieuGiamGiaId") Long phieuGiamGiaId);
    
    // Lấy phiếu giảm giá cá nhân theo khách hàng với phân trang
    Page<PhieuGiamGiaCaNhan> findByKhachHangId(Long khachHangId, Pageable pageable);
}