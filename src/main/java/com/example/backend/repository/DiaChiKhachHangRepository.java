package com.example.backend.repository;

import com.example.backend.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Long> {
    
    // Lấy tất cả địa chỉ của một khách hàng
    List<DiaChiKhachHang> findByKhachHangIdAndTrangThaiTrueOrderByMacDinhDescNgayTaoDesc(Long khachHangId);
    
    // Lấy địa chỉ mặc định của khách hàng
    Optional<DiaChiKhachHang> findByKhachHangIdAndMacDinhTrueAndTrangThaiTrue(Long khachHangId);
    
    // Đếm số địa chỉ của khách hàng
    long countByKhachHangIdAndTrangThaiTrue(Long khachHangId);
    
    // Kiểm tra xem khách hàng có địa chỉ mặc định không
    boolean existsByKhachHangIdAndMacDinhTrueAndTrangThaiTrue(Long khachHangId);
    
    // Lấy địa chỉ theo ID và khách hàng ID (để đảm bảo bảo mật)
    Optional<DiaChiKhachHang> findByIdAndKhachHangId(Long id, Long khachHangId);
    
    // Cập nhật tất cả địa chỉ của khách hàng thành không mặc định
    @Query("UPDATE DiaChiKhachHang d SET d.macDinh = false WHERE d.khachHang.id = :khachHangId")
    void updateAllAddressesToNonDefault(@Param("khachHangId") Long khachHangId);
    
    // Lấy tất cả địa chỉ (cho hiển thị bảng)
    List<DiaChiKhachHang> findByTrangThaiTrueOrderByKhachHangIdAscMacDinhDescNgayTaoDesc();
}
