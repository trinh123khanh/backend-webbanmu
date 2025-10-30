package com.example.backend.repository;

import com.example.backend.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;
@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Long> {
    @Query("SELECT c FROM ChiTietSanPham c JOIN c.sanPham s WHERE s.tenSanPham = :tenSanPham")
    Optional<ChiTietSanPham> findByTenSanPham(@Param("tenSanPham") String tenSanPham);

    // Find all ChiTietSanPham by linked SanPham ID
    List<ChiTietSanPham> findBySanPhamId(Long sanPhamId);
}
