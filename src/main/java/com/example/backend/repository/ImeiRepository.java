package com.example.backend.repository;

import com.example.backend.entity.Imei;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImeiRepository extends JpaRepository<Imei, Long> {
    
    // Tìm IMEI theo số IMEI
    Optional<Imei> findBySoImei(String soImei);
    
    // Kiểm tra IMEI có tồn tại không
    boolean existsBySoImei(String soImei);
    
    // Tìm tất cả IMEI theo sản phẩm
    List<Imei> findBySanPhamId(Long sanPhamId);
    
    // Tìm IMEI theo sản phẩm với phân trang
    Page<Imei> findBySanPhamId(Long sanPhamId, Pageable pageable);
    
    // Tìm IMEI còn hàng theo sản phẩm
    List<Imei> findBySanPhamIdAndTrangThaiTrue(Long sanPhamId);
    
    // Đếm số IMEI còn hàng theo sản phẩm
    long countBySanPhamIdAndTrangThaiTrue(Long sanPhamId);
    
    // Đếm số IMEI còn hàng toàn hệ thống
    long countByTrangThaiTrue();
    
    // Tìm IMEI theo trạng thái
    List<Imei> findByTrangThai(Boolean trangThai);
    
    // Tìm kiếm IMEI theo số IMEI (partial match)
    @Query("SELECT i FROM Imei i WHERE i.soImei LIKE %:soImei%")
    List<Imei> findBySoImeiContaining(@Param("soImei") String soImei);
    
    // Tìm IMEI theo sản phẩm và trạng thái
    List<Imei> findBySanPhamIdAndTrangThai(Long sanPhamId, Boolean trangThai);
    
    // Xóa tất cả IMEI của một sản phẩm
    void deleteBySanPhamId(Long sanPhamId);
}
