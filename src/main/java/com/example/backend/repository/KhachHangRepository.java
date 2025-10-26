package com.example.backend.repository;

import com.example.backend.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    
    // Tìm khách hàng theo email
    Optional<KhachHang> findByEmail(String email);
    
    // Tìm khách hàng theo số điện thoại
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    
    // Tìm khách hàng theo mã khách hàng
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);
    
    // Kiểm tra mã khách hàng đã tồn tại chưa
    boolean existsByMaKhachHang(String maKhachHang);
    
    // Tìm khách hàng theo tên (tìm kiếm gần đúng)
    @Query("SELECT k FROM KhachHang k WHERE LOWER(k.tenKhachHang) LIKE LOWER(CONCAT('%', :tenKhachHang, '%'))")
    List<KhachHang> findByTenKhachHangContainingIgnoreCase(@Param("tenKhachHang") String tenKhachHang);
    
    // Tìm khách hàng theo trạng thái
    List<KhachHang> findByTrangThai(Boolean trangThai);
    
    // Tìm kiếm tổng hợp với phân trang
    @Query("SELECT k FROM KhachHang k WHERE " +
           "(:maKhachHang IS NULL OR LOWER(k.maKhachHang) LIKE LOWER(CONCAT('%', :maKhachHang, '%'))) AND " +
           "(:tenKhachHang IS NULL OR LOWER(k.tenKhachHang) LIKE LOWER(CONCAT('%', :tenKhachHang, '%'))) AND " +
           "(:email IS NULL OR LOWER(k.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:soDienThoai IS NULL OR k.soDienThoai LIKE CONCAT('%', :soDienThoai, '%')) AND " +
           "(:trangThai IS NULL OR k.trangThai = :trangThai)")
    Page<KhachHang> findWithFilters(@Param("maKhachHang") String maKhachHang,
                                   @Param("tenKhachHang") String tenKhachHang,
                                   @Param("email") String email,
                                   @Param("soDienThoai") String soDienThoai,
                                   @Param("trangThai") Boolean trangThai,
                                   Pageable pageable);
    
    // Đếm số lượng khách hàng theo trạng thái
    long countByTrangThai(Boolean trangThai);
    
    // Tìm khách hàng có điểm tích lũy cao nhất
    @Query("SELECT k FROM KhachHang k ORDER BY k.diemTichLuy DESC")
    List<KhachHang> findTopByDiemTichLuy(Pageable pageable);
    
    // Tìm khách hàng theo khoảng thời gian tạo
    @Query("SELECT k FROM KhachHang k WHERE k.ngayTao BETWEEN :startDate AND :endDate")
    List<KhachHang> findByNgayTaoBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
}