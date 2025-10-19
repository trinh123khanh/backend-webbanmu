package com.example.backend.repository;

import com.example.backend.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    
    // Tìm kiếm theo mã khách hàng
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);
    
    // Tìm kiếm theo email
    Optional<KhachHang> findByEmail(String email);
    
    // Tìm kiếm theo số điện thoại
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    
    // Tìm kiếm theo tên khách hàng
    List<KhachHang> findByTenKhachHangContainingIgnoreCase(String tenKhachHang);
    
    // Tìm kiếm theo trạng thái
    List<KhachHang> findByTrangThai(Boolean trangThai);
    
    // Tìm kiếm theo user ID
    Optional<KhachHang> findByUserId(Long userId);
    
    // Kiểm tra email đã tồn tại (trừ khách hàng hiện tại)
    @Query("SELECT COUNT(k) > 0 FROM KhachHang k WHERE k.email = :email AND k.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    // Kiểm tra số điện thoại đã tồn tại (trừ khách hàng hiện tại)
    @Query("SELECT COUNT(k) > 0 FROM KhachHang k WHERE k.soDienThoai = :soDienThoai AND k.id != :id")
    boolean existsBySoDienThoaiAndIdNot(@Param("soDienThoai") String soDienThoai, @Param("id") Long id);
    
    // Kiểm tra mã khách hàng đã tồn tại (trừ khách hàng hiện tại)
    @Query("SELECT COUNT(k) > 0 FROM KhachHang k WHERE k.maKhachHang = :maKhachHang AND k.id != :id")
    boolean existsByMaKhachHangAndIdNot(@Param("maKhachHang") String maKhachHang, @Param("id") Long id);
    
    // Tìm kiếm tổng hợp với phân trang
    @Query("SELECT k FROM KhachHang k WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(k.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR k.trangThai = :trangThai)")
    Page<KhachHang> findWithFilters(@Param("keyword") String keyword, 
                                   @Param("trangThai") Boolean trangThai, 
                                   Pageable pageable);
    
    // Đếm số khách hàng theo trạng thái
    long countByTrangThai(Boolean trangThai);
}

