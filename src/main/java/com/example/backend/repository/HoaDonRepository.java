package com.example.backend.repository;

import com.example.backend.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    List<HoaDon> findByTrangThai(HoaDon.TrangThaiHoaDon trangThai);

    List<HoaDon> findByNgayTaoBetween(LocalDateTime startDate, LocalDateTime endDate);

    // A more comprehensive query for searching and filtering
    @Query("SELECT h FROM HoaDon h WHERE " +
           "(:search IS NULL OR h.maHoaDon LIKE %:search% OR h.ghiChu LIKE %:search%) AND " +
           "(:trangThai IS NULL OR h.trangThai = :trangThai)")
    Page<HoaDon> searchAndFilter(
        @Param("search") String search,
        @Param("trangThai") HoaDon.TrangThaiHoaDon trangThai,
        Pageable pageable
    );
}
