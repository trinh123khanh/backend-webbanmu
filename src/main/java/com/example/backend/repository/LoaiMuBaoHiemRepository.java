package com.example.backend.repository;

import com.example.backend.entity.LoaiMuBaoHiem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoaiMuBaoHiemRepository extends JpaRepository<LoaiMuBaoHiem, Long> {

    // Kiểm tra tên loại mũ đã tồn tại chưa (không phân biệt hoa thường)
    boolean existsByTenLoaiIgnoreCase(String tenLoai);

    // Tìm kiếm với keyword và trạng thái
    @Query("SELECT l FROM LoaiMuBaoHiem l WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(l.tenLoai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.moTa) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR l.trangThai = :trangThai)")
    Page<LoaiMuBaoHiem> search(@Param("keyword") String keyword,
                               @Param("trangThai") Boolean trangThai,
                               Pageable pageable);

    // Lấy tất cả loại mũ đang hoạt động
    List<LoaiMuBaoHiem> findByTrangThaiTrue();

    // Lấy loại mũ theo tên (không phân biệt hoa thường)
    Optional<LoaiMuBaoHiem> findByTenLoaiIgnoreCase(String tenLoai);
}


