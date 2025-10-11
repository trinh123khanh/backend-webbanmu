package com.example.backend.repository;

import com.example.backend.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    
    Optional<KhachHang> findByEmail(String email);
    
    List<KhachHang> findBySoDienThoai(String soDienThoai);
    
    List<KhachHang> findByTenKhachHangContainingIgnoreCase(String name);
    
    Optional<KhachHang> findByTenKhachHangIgnoreCase(String name);
}
