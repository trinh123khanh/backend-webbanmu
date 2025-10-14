package com.example.backend.repository;

import com.example.backend.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    
    Optional<KhachHang> findByEmail(String email);
    
    List<KhachHang> findBySoDienThoai(String soDienThoai);
    
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);
    
    List<KhachHang> findByTrangThai(Boolean trangThai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.tenKhachHang LIKE %:ten%")
    List<KhachHang> findByTenKhachHangContaining(@Param("ten") String ten);
    
    @Query("SELECT k FROM KhachHang k WHERE k.email LIKE %:email%")
    List<KhachHang> findByEmailContaining(@Param("email") String email);
    
    @Query("SELECT k FROM KhachHang k WHERE k.soDienThoai LIKE %:soDienThoai%")
    List<KhachHang> findBySoDienThoaiContaining(@Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.userId = :userId")
    Optional<KhachHang> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT k FROM KhachHang k ORDER BY k.diemTichLuy DESC")
    List<KhachHang> findAllOrderByDiemTichLuyDesc();
    
    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = true ORDER BY k.ngayTao DESC")
    List<KhachHang> findActiveCustomersOrderByNgayTaoDesc();
    
    @Query(value = "SELECT * FROM khach_hang", nativeQuery = true)
    List<KhachHang> findAllNative();
    
    // Tìm khách hàng theo tên và trạng thái
    @Query("SELECT k FROM KhachHang k WHERE k.tenKhachHang LIKE %:ten% AND k.trangThai = :trangThai")
    List<KhachHang> findByTenKhachHangContainingAndTrangThai(@Param("ten") String ten, @Param("trangThai") Boolean trangThai);
    
    // Đếm số lượng khách hàng theo trạng thái
    Long countByTrangThai(Boolean trangThai);
}
