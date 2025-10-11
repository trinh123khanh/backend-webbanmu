package com.example.backend.repository;

import com.example.backend.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Long> {
    
    // Tìm phiếu giảm giá theo mã phiếu
    Optional<PhieuGiamGia> findByMaPhieu(String maPhieu);
    
    // Kiểm tra mã phiếu đã tồn tại chưa
    boolean existsByMaPhieu(String maPhieu);
    
    // Tìm phiếu giảm giá theo loại
    List<PhieuGiamGia> findByLoaiPhieuGiamGia(Boolean loaiPhieuGiamGia);
    
    // Tìm phiếu giảm giá theo trạng thái
    List<PhieuGiamGia> findByTrangThai(Boolean trangThai);
    
    // Tìm phiếu giảm giá đang hoạt động
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = true " +
           "AND p.ngayBatDau <= :currentDate AND p.ngayKetThuc >= :currentDate")
    List<PhieuGiamGia> findActiveVouchers(@Param("currentDate") LocalDate currentDate);
    
    // Tìm phiếu giảm giá sắp hết hạn (trong vòng 7 ngày)
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = true " +
           "AND p.ngayKetThuc BETWEEN :startDate AND :endDate")
    List<PhieuGiamGia> findExpiringSoon(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    // Tìm phiếu giảm giá đã hết hạn
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.ngayKetThuc < :currentDate")
    List<PhieuGiamGia> findExpiredVouchers(@Param("currentDate") LocalDate currentDate);
    
    // Tìm phiếu giảm giá chưa bắt đầu
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.ngayBatDau > :currentDate")
    List<PhieuGiamGia> findNotStartedVouchers(@Param("currentDate") LocalDate currentDate);
    
    // Đếm số lượng phiếu giảm giá đang hoạt động
    @Query("SELECT COUNT(p) FROM PhieuGiamGia p WHERE p.trangThai = true " +
           "AND p.ngayBatDau <= :currentDate AND p.ngayKetThuc >= :currentDate")
    long countActiveVouchers(@Param("currentDate") LocalDate currentDate);
    
    // Tìm kiếm phiếu giảm giá theo từ khóa
    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
           "LOWER(p.maPhieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.tenPhieuGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PhieuGiamGia> searchByKeyword(@Param("keyword") String keyword);
    
    // Tìm phiếu giảm giá theo khoảng giá trị giảm
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.giaTriGiam BETWEEN :minValue AND :maxValue")
    List<PhieuGiamGia> findByGiaTriGiamRange(@Param("minValue") java.math.BigDecimal minValue, 
                                            @Param("maxValue") java.math.BigDecimal maxValue);
    
    // Tìm phiếu giảm giá theo khoảng thời gian
    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
           "p.ngayBatDau >= :startDate AND p.ngayKetThuc <= :endDate")
    List<PhieuGiamGia> findByDateRange(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
}
