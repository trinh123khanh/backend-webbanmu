package com.example.backend.repository;

import com.example.backend.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Long> {
    // Lấy danh sách chi tiết theo sản phẩm
    java.util.List<ChiTietSanPham> findBySanPham_Id(Long sanPhamId);
}
