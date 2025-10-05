package com.example.backend.repository;

import com.example.backend.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    
    Optional<NhanVien> findByEmail(String email);
    
    Optional<NhanVien> findBySoDienThoai(String soDienThoai);
    
    List<NhanVien> findByHoTen(String hoTen);
}
