package com.example.backend.repository;

import com.example.backend.entity.TrongLuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TrongLuongRepository extends JpaRepository<TrongLuong, Long> {
    
    // Kiểm tra trọng lượng đã tồn tại chưa (theo giá trị và đơn vị)
    boolean existsByGiaTriTrongLuongAndDonVi(BigDecimal giaTriTrongLuong, String donVi);
    
    // Tìm kiếm với keyword và trạng thái
    @Query("SELECT t FROM TrongLuong t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(t.donVi) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.moTa) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR t.trangThai = :trangThai)")
    Page<TrongLuong> search(@Param("keyword") String keyword, 
                           @Param("trangThai") Boolean trangThai, 
                           Pageable pageable);
    
    // Lấy tất cả trọng lượng đang hoạt động
    List<TrongLuong> findByTrangThaiTrue();
    
    // Lấy tất cả trọng lượng theo trạng thái
    List<TrongLuong> findByTrangThai(Boolean trangThai);
}