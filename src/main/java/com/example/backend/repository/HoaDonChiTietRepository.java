package com.example.backend.repository;

import com.example.backend.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Long> {
    
       
    @Query("SELECT DISTINCT hdct FROM HoaDonChiTiet hdct " +
           "JOIN FETCH hdct.hoaDon hd " +
           "JOIN FETCH hdct.chiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.sanPham sp " +
           "LEFT JOIN FETCH ctsp.mauSac ms " +
           "LEFT JOIN FETCH sp.kieuDangMu kdm " +
           "LEFT JOIN FETCH ctsp.kichThuoc kt " +
           "LEFT JOIN FETCH sp.nhaSanXuat nsx " +
           "WHERE hd.ngayTao BETWEEN :startDate AND :endDate " +
           "AND hd.trangThai != 'DA_HUY'")
    List<HoaDonChiTiet> findWithProductDetailsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Query đơn giản hơn để test - lấy tất cả không filter theo thời gian
    // Thử dùng enum thay vì string
    @Query("SELECT DISTINCT hdct FROM HoaDonChiTiet hdct " +
           "JOIN FETCH hdct.hoaDon hd " +
           "JOIN FETCH hdct.chiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.sanPham sp " +
           "LEFT JOIN FETCH ctsp.mauSac ms " +
           "LEFT JOIN FETCH sp.kieuDangMu kdm " +
           "LEFT JOIN FETCH sp.nhaSanXuat nsx " +
           "WHERE hd.trangThai != com.example.backend.entity.HoaDon$TrangThaiHoaDon.DA_HUY " +
           "ORDER BY hd.ngayTao DESC")
    List<HoaDonChiTiet> findAllWithProductDetailsExcludingCancelled();
    
    // Query backup - lấy các trạng thái cụ thể thay vì loại trừ DA_HUY
    @Query("SELECT DISTINCT hdct FROM HoaDonChiTiet hdct " +
           "JOIN FETCH hdct.hoaDon hd " +
           "JOIN FETCH hdct.chiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.sanPham sp " +
           "LEFT JOIN FETCH ctsp.mauSac ms " +
           "LEFT JOIN FETCH sp.kieuDangMu kdm " +
           "LEFT JOIN FETCH sp.nhaSanXuat nsx " +
           "WHERE hd.trangThai IN (com.example.backend.entity.HoaDon$TrangThaiHoaDon.CHO_XAC_NHAN, " +
           "                        com.example.backend.entity.HoaDon$TrangThaiHoaDon.CHO_VAN_CHUYEN, " +
           "                        com.example.backend.entity.HoaDon$TrangThaiHoaDon.DANG_GIAO_HANG, " +
           "                        com.example.backend.entity.HoaDon$TrangThaiHoaDon.DA_GIAO_HANG) " +
           "ORDER BY hd.ngayTao DESC")
    List<HoaDonChiTiet> findAllWithProductDetailsExcludingCancelledBackup();
    
    // Query để đếm tổng số bản ghi
    @Query("SELECT COUNT(DISTINCT hdct) FROM HoaDonChiTiet hdct " +
           "JOIN hdct.hoaDon hd " +
           "WHERE hd.trangThai != 'DA_HUY'")
    long countAllExcludingCancelled();
    
    // Query với JOIN FETCH để load đầy đủ relationships
    @Query("SELECT DISTINCT hdct FROM HoaDonChiTiet hdct " +
           "LEFT JOIN FETCH hdct.chiTietSanPham ct " +
           "LEFT JOIN FETCH ct.sanPham s " +
           "LEFT JOIN FETCH s.nhaSanXuat " +
           "LEFT JOIN FETCH s.loaiMuBaoHiem " +
           "LEFT JOIN FETCH ct.mauSac " +
           "LEFT JOIN FETCH ct.kichThuoc " +
           "WHERE hdct.hoaDon.id = :hoaDonId")
    List<HoaDonChiTiet> findByHoaDonId(@Param("hoaDonId") Long hoaDonId);
    
    // Query đơn giản hơn - chỉ lấy theo hoa_don_id (không JOIN FETCH)
    // Dùng khi query với JOIN FETCH không hoạt động
    @Query("SELECT hdct FROM HoaDonChiTiet hdct WHERE hdct.hoaDon.id = :hoaDonId")
    List<HoaDonChiTiet> findByHoaDonIdSimple(@Param("hoaDonId") Long hoaDonId);
    
    // Query đơn giản nhất - lấy tất cả không filter gì cả (để test)
    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
           "JOIN FETCH hdct.hoaDon hd " +
           "JOIN FETCH hdct.chiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.sanPham sp " +
           "LEFT JOIN FETCH ctsp.mauSac ms " +
           "LEFT JOIN FETCH sp.kieuDangMu kdm " +
           "LEFT JOIN FETCH sp.nhaSanXuat nsx")
    List<HoaDonChiTiet> findAllWithAllDetails();
}

