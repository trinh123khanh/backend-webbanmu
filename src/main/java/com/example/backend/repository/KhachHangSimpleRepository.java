package com.example.backend.repository;

import com.example.backend.entity.KhachHangSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhachHangSimpleRepository extends JpaRepository<KhachHangSimple, Long> {
    List<KhachHangSimple> findAll();
}
