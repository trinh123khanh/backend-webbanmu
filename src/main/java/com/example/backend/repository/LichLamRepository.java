package com.example.backend.repository;

import com.example.backend.entity.lich_lam;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LichLamRepository extends JpaRepository<lich_lam, Long> {
    
    // Tìm lịch làm theo user và tuần/năm
    @Query("SELECT l FROM lich_lam l WHERE l.user.id = :userId AND l.date LIKE :datePattern")
    List<lich_lam> findByUserIdAndDatePattern(@Param("userId") Long userId, @Param("datePattern") String datePattern);
    
    // Tìm lịch làm theo user
    List<lich_lam> findByUser(User user);
    
    // Tìm lịch làm theo user và ngày cụ thể
    List<lich_lam> findByUserAndDate(User user, String date);
    
    // Xóa lịch làm theo user và tuần (dựa trên pattern ngày)
    @Modifying
    @Transactional
    @Query("DELETE FROM lich_lam l WHERE l.user.id = :userId AND l.date LIKE :datePattern")
    void deleteByUserIdAndDatePattern(@Param("userId") Long userId, @Param("datePattern") String datePattern);
    
    // Lấy tất cả lịch làm theo pattern ngày (để lấy theo tuần)
    @Query("SELECT l FROM lich_lam l WHERE l.date LIKE :datePattern ORDER BY l.user.id, l.dayOfWeek")
    List<lich_lam> findByDatePattern(@Param("datePattern") String datePattern);
    
    // Lấy lịch sử ca làm (tất cả hoặc theo user)
    @Query("SELECT l FROM lich_lam l WHERE l.user.id = :userId ORDER BY l.date DESC, l.dayOfWeek")
    List<lich_lam> findHistoryByUserId(@Param("userId") Long userId);
    
    // Lấy tất cả lịch sử
    @Query("SELECT l FROM lich_lam l ORDER BY l.date DESC, l.dayOfWeek")
    List<lich_lam> findAllHistory();
}

