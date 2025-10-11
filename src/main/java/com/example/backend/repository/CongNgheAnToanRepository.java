package com.example.backend.repository;

import com.example.backend.entity.CongNgheAnToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CongNgheAnToanRepository extends JpaRepository<CongNgheAnToan, Long> {
    
    boolean existsByTenCongNghe(String tenCongNghe);
    
    @Query("SELECT c FROM CongNgheAnToan c WHERE (:keyword IS NULL OR LOWER(c.tenCongNghe) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR c.trangThai = :trangThai)")
    Page<CongNgheAnToan> search(@Param("keyword") String keyword,
                                @Param("trangThai") Boolean trangThai,
                                Pageable pageable);
    
    @Query(value = "SELECT setval('cong_nghe_an_toan_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM cong_nghe_an_toan))", nativeQuery = true)
    void fixSequence();
}