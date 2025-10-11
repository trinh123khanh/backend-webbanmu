package com.example.backend.repository;

import com.example.backend.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    
    Optional<SanPham> findByMaSanPham(String maSanPham);
    
    List<SanPham> findByTenSanPhamContainingIgnoreCase(String tenSanPham);
    
    List<SanPham> findByDanhMuc(String danhMuc);
    
    List<SanPham> findByThuongHieu(String thuongHieu);
    
    List<SanPham> findByTrangThai(Boolean trangThai);
    
    @Query("SELECT s FROM SanPham s WHERE s.soLuongTon > 0 AND s.trangThai = true")
    List<SanPham> findAvailableProducts();
    
    @Query("SELECT s FROM SanPham s WHERE " +
           "LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.maSanPham) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.danhMuc) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.thuongHieu) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SanPham> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = true ORDER BY s.tenSanPham ASC")
    List<SanPham> findAllActiveProducts();
}
