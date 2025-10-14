package com.example.backend.repository;

import com.example.backend.entity.PhieuGiamGiaCaNhan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Long> {
    
    // Lấy tất cả phiếu giảm giá cá nhân với thông tin liên quan
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p LEFT JOIN FETCH p.phieuGiamGia")
    List<PhieuGiamGiaCaNhan> findAllWithPhieuGiamGia();
    
    // Lấy tất cả với phân trang
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p LEFT JOIN FETCH p.phieuGiamGia")
    Page<PhieuGiamGiaCaNhan> findAllWithPhieuGiamGia(Pageable pageable);
    
    // Lấy phiếu giảm giá cá nhân theo khách hàng
    List<PhieuGiamGiaCaNhan> findByKhachHangId(Long khachHangId);
    
    // Lấy phiếu giảm giá cá nhân theo khách hàng với phân trang
    Page<PhieuGiamGiaCaNhan> findByKhachHangId(Long khachHangId, Pageable pageable);
    
    // Lấy phiếu giảm giá cá nhân theo phiếu giảm giá
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGiaId(Long phieuGiamGiaId);
    
    // Lấy phiếu giảm giá cá nhân có thể sử dụng
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.daSuDung = false AND p.ngayHetHan > CURRENT_TIMESTAMP")
    List<PhieuGiamGiaCaNhan> findAvailableVouchers();
    
    // Lấy phiếu giảm giá cá nhân có thể sử dụng của khách hàng
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.khachHangId = :khachHangId AND p.daSuDung = false AND p.ngayHetHan > CURRENT_TIMESTAMP")
    List<PhieuGiamGiaCaNhan> findAvailableVouchersByKhachHang(@Param("khachHangId") Long khachHangId);
    
    // Lấy phiếu giảm giá cá nhân đã sử dụng
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.daSuDung = true")
    List<PhieuGiamGiaCaNhan> findUsedVouchers();
    
    // Lấy phiếu giảm giá cá nhân đã hết hạn
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.daSuDung = false AND p.ngayHetHan <= CURRENT_TIMESTAMP")
    List<PhieuGiamGiaCaNhan> findExpiredVouchers();
    
    // Đếm số phiếu giảm giá cá nhân theo khách hàng
    Long countByKhachHangId(Long khachHangId);
    
    // Đếm số phiếu giảm giá cá nhân đã sử dụng theo khách hàng
    @Query("SELECT COUNT(p) FROM PhieuGiamGiaCaNhan p WHERE p.khachHangId = :khachHangId AND p.daSuDung = true")
    Long countUsedVouchersByKhachHang(@Param("khachHangId") Long khachHangId);
    
    // Đếm số phiếu giảm giá cá nhân có thể sử dụng theo khách hàng
    @Query("SELECT COUNT(p) FROM PhieuGiamGiaCaNhan p WHERE p.khachHangId = :khachHangId AND p.daSuDung = false AND p.ngayHetHan > CURRENT_TIMESTAMP")
    Long countAvailableVouchersByKhachHang(@Param("khachHangId") Long khachHangId);
    
    // Tìm phiếu giảm giá cá nhân theo ID với thông tin liên quan
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p LEFT JOIN FETCH p.phieuGiamGia WHERE p.id = :id")
    Optional<PhieuGiamGiaCaNhan> findByIdWithPhieuGiamGia(@Param("id") Long id);
    
    // Kiểm tra xem khách hàng đã có phiếu giảm giá này chưa
    boolean existsByKhachHangIdAndPhieuGiamGiaId(Long khachHangId, Long phieuGiamGiaId);
    
    // Lấy phiếu giảm giá cá nhân trong khoảng thời gian
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.ngayHetHan BETWEEN :startDate AND :endDate")
    List<PhieuGiamGiaCaNhan> findByNgayHetHanBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
