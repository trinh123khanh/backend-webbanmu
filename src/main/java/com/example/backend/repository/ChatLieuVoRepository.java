package com.example.backend.repository;

import com.example.backend.entity.ChatLieuVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatLieuVoRepository extends JpaRepository<ChatLieuVo, Long> {
    boolean existsByTenChatLieu(String tenChatLieu);

    @Query("SELECT c FROM ChatLieuVo c WHERE (:keyword IS NULL OR LOWER(c.tenChatLieu) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:trangThai IS NULL OR c.trangThai = :trangThai)")
    Page<ChatLieuVo> search(@Param("keyword") String keyword,
                            @Param("trangThai") Boolean trangThai,
                            Pageable pageable);
}


