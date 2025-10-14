package com.example.backend.repository;

import com.example.backend.entity.NhanVien;
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
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    
    Optional<NhanVien> findByEmail(String email);
    
    Optional<NhanVien> findBySoDienThoai(String soDienThoai);
    
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);
    
    List<NhanVien> findByHoTen(String hoTen);
    
    List<NhanVien> findByTrangThai(Boolean trangThai);
    
    List<NhanVien> findByNgayVaoLamBetween(LocalDate startDate, LocalDate endDate);
    
    long countByTrangThai(Boolean trangThai);
    
    // Tìm kiếm với bộ lọc
    @Query("SELECT n FROM NhanVien n WHERE " +
           "(:hoTen IS NULL OR LOWER(n.hoTen) LIKE LOWER(CONCAT('%', :hoTen, '%'))) AND " +
           "(:email IS NULL OR LOWER(n.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:soDienThoai IS NULL OR n.soDienThoai LIKE CONCAT('%', :soDienThoai, '%')) AND " +
           "(:maNhanVien IS NULL OR LOWER(n.maNhanVien) LIKE LOWER(CONCAT('%', :maNhanVien, '%'))) AND " +
           "(:trangThai IS NULL OR n.trangThai = :trangThai)")
    Page<NhanVien> findWithFilters(@Param("hoTen") String hoTen,
                                   @Param("email") String email,
                                   @Param("soDienThoai") String soDienThoai,
                                   @Param("maNhanVien") String maNhanVien,
                                   @Param("trangThai") Boolean trangThai,
                                   Pageable pageable);
    
    // Tìm nhân viên theo khoảng thời gian vào làm
    @Query("SELECT n FROM NhanVien n WHERE n.ngayVaoLam BETWEEN :startDate AND :endDate")
    List<NhanVien> findByNgayVaoLamRange(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
}
