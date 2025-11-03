package com.example.backend.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(1) // Chạy trước TestDataInitializer
public class FixUserRolesConstraintInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("🔧 Đang sửa constraint user_roles_roles_check...");
            
            // Drop existing constraint if it exists
            String dropConstraintSql = 
                "ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_roles_check";
            entityManager.createNativeQuery(dropConstraintSql).executeUpdate();
            log.info("✅ Đã xóa constraint cũ (nếu có)");
            
            // Create new constraint that allows ADMIN, STAFF, CUSTOMER
            String addConstraintSql = 
                "ALTER TABLE user_roles " +
                "ADD CONSTRAINT user_roles_roles_check " +
                "CHECK (roles IN ('ADMIN', 'STAFF', 'CUSTOMER'))";
            
            entityManager.createNativeQuery(addConstraintSql).executeUpdate();
            log.info("✅ Đã tạo constraint mới cho phép ADMIN, STAFF, CUSTOMER");
        } catch (Exception e) {
            // Nếu constraint đã tồn tại, bỏ qua
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                log.info("ℹ️  Constraint đã tồn tại, bỏ qua");
            } else {
                log.error("❌ Lỗi khi cập nhật constraint: {}", e.getMessage());
                // Không throw exception để app vẫn có thể chạy
                // Nhưng sẽ log lỗi để debug
            }
        }
    }
}

