package com.example.backend.repository;

import com.example.backend.entity.DotGiamGia;
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
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Long> {
    
    // Tìm theo mã đợt giảm giá
    Optional<DotGiamGia> findByMaDotGiamGia(String maDotGiamGia);
    
    // Kiểm tra mã đợt giảm giá đã tồn tại chưa (trừ id hiện tại)
    boolean existsByMaDotGiamGiaAndIdNot(String maDotGiamGia, Long id);
    
    // Kiểm tra mã đợt giảm giá đã tồn tại chưa
    boolean existsByMaDotGiamGia(String maDotGiamGia);
    
    // Tìm theo trạng thái
    List<DotGiamGia> findByTrangThai(Boolean trangThai);
    
    // Tìm theo loại đợt giảm giá
    List<DotGiamGia> findByLoaiDotGiamGia(String loaiDotGiamGia);
    
    // Tìm các đợt giảm giá đang hoạt động (trong thời gian hiệu lực)
    @Query("SELECT d FROM DotGiamGia d WHERE d.trangThai = true AND d.ngayBatDau <= :currentTime AND d.ngayKetThuc >= :currentTime")
    List<DotGiamGia> findActiveDiscounts(@Param("currentTime") LocalDateTime currentTime);



    //jkmgvhbjknhvkljhvnklhj

    // Tìm theo tên đợt giảm giá (tìm kiếm gần đúng)
    @Query("SELECT d FROM DotGiamGia d WHERE LOWER(d.tenDotGiamGia) LIKE LOWER(CONCAT('%', :tenDotGiamGia, '%'))")
    Page<DotGiamGia> findByTenDotGiamGiaContaining(@Param("tenDotGiamGia") String tenDotGiamGia, Pageable pageable);
    
    // Tìm theo mã đợt giảm giá (tìm kiếm gần đúng)
    @Query("SELECT d FROM DotGiamGia d WHERE LOWER(d.maDotGiamGia) LIKE LOWER(CONCAT('%', :maDotGiamGia, '%'))")
    Page<DotGiamGia> findByMaDotGiamGiaContaining(@Param("maDotGiamGia") String maDotGiamGia, Pageable pageable);
    
    // Tìm kiếm tổng hợp
    @Query("SELECT d FROM DotGiamGia d WHERE " +
           "(:tenDotGiamGia IS NULL OR LOWER(d.tenDotGiamGia) LIKE LOWER(CONCAT('%', :tenDotGiamGia, '%'))) AND " +
           "(:maDotGiamGia IS NULL OR LOWER(d.maDotGiamGia) LIKE LOWER(CONCAT('%', :maDotGiamGia, '%'))) AND " +
           "(:trangThai IS NULL OR d.trangThai = :trangThai) AND " +
           "(:loaiDotGiamGia IS NULL OR d.loaiDotGiamGia = :loaiDotGiamGia)")
    Page<DotGiamGia> searchDotGiamGia(@Param("tenDotGiamGia") String tenDotGiamGia,
                                     @Param("maDotGiamGia") String maDotGiamGia,
                                     @Param("trangThai") Boolean trangThai,
                                     @Param("loaiDotGiamGia") String loaiDotGiamGia,
                                     Pageable pageable);
}
