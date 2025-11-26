package com.example.backend.repository;

import com.example.backend.entity.HoaDonCho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChoRepository extends JpaRepository<HoaDonCho, Long> {
    
    Optional<HoaDonCho> findByMaHoaDonCho(String maHoaDonCho);
    
    List<HoaDonCho> findByTrangThai(String trangThai);
    
    long countByTrangThai(String trangThai);
    
    List<HoaDonCho> findByKhachHangId(Long khachHangId);
    
    // Lấy giỏ hàng ONLINE (nhanVienId = null) theo khách hàng
    @Query("SELECT h FROM HoaDonCho h WHERE h.khachHang.id = :khachHangId AND h.nhanVien IS NULL")
    List<HoaDonCho> findByKhachHangIdAndNhanVienIsNull(@Param("khachHangId") Long khachHangId);
    
    // Lấy giỏ hàng TẠI QUẦY (nhanVienId != null) theo nhân viên
    List<HoaDonCho> findByNhanVienId(Long nhanVienId);
    
    @Query("SELECT h FROM HoaDonCho h ORDER BY h.ngayTao DESC")
    List<HoaDonCho> findAllOrderByNgayTaoDesc();
    
    // Fetch HoaDonCho with gioHangCho collection
    @Query("SELECT DISTINCT h FROM HoaDonCho h " +
           "LEFT JOIN FETCH h.gioHangCho g " +
           "LEFT JOIN FETCH g.chiTietSanPham " +
           "WHERE h.id = :id")
    Optional<HoaDonCho> findByIdWithGioHangCho(@Param("id") Long id);
}

