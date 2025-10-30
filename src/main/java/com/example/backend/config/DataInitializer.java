package com.example.backend.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            // Kiểm tra xem cột ma_nhan_vien có tồn tại không
            String checkColumnSql = "SELECT COUNT(*) FROM information_schema.columns " +
                                   "WHERE table_name = 'nhan_vien' AND column_name = 'ma_nhan_vien'";
            
            Object result = entityManager.createNativeQuery(checkColumnSql).getSingleResult();
            Long count = ((Number) result).longValue();
            
            if (count > 0) {
                // Nếu cột tồn tại, cập nhật các bản ghi NULL
                String updateSql = "UPDATE nhan_vien " +
                                 "SET ma_nhan_vien = 'NV' || LPAD(id::text, 4, '0') " +
                                 "WHERE ma_nhan_vien IS NULL";
                
                int updated = entityManager.createNativeQuery(updateSql).executeUpdate();
                
                if (updated > 0) {
                    System.out.println("Updated " + updated + " records with NULL ma_nhan_vien");
                }
            }
        } catch (Exception e) {
            // Không throw exception để không làm ảnh hưởng đến việc start application
            System.out.println("Could not update ma_nhan_vien: " + e.getMessage());
        }
    }
}

