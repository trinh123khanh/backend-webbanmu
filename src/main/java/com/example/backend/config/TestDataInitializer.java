package com.example.backend.config;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Chạy sau FixUserRolesConstraintInitializer
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Chỉ chạy trong môi trường development
        String env = System.getProperty("spring.profiles.active", "");
        if ("production".equals(env)) {
            log.info("TestDataInitializer skipped in production environment");
            return;
        }

        log.info("🔧 Đang tạo tài khoản test...");

        // 1. Admin User
        createUserIfNotExists(
            "admin",
            "admin123",
            "admin@tdkstore.com",
            "Quản Trị Viên",
            User.UserRole.ADMIN
        );

        // 2. Staff User
        createUserIfNotExists(
            "staff",
            "staff123",
            "staff@tdkstore.com",
            "Nhân Viên",
            User.UserRole.STAFF
        );

        // 3. Customer User 1
        createUserIfNotExists(
            "customer1",
            "customer123",
            "customer1@tdkstore.com",
            "Khách Hàng 1",
            User.UserRole.CUSTOMER
        );

        // 4. Customer User 2
        createUserIfNotExists(
            "customer2",
            "customer123",
            "customer2@tdkstore.com",
            "Khách Hàng 2",
            User.UserRole.CUSTOMER
        );

        log.info("✅ Hoàn thành tạo tài khoản test!");
        log.info("📝 Thông tin đăng nhập:");
        log.info("   👨‍💼 ADMIN:    username=admin,    password=admin123");
        log.info("   👨‍💻 STAFF:    username=staff,    password=staff123");
        log.info("   🧑 CUSTOMER1: username=customer1, password=customer123");
        log.info("   🧑 CUSTOMER2: username=customer2, password=customer123");
    }

    private void createUserIfNotExists(
            String username,
            String password,
            String email,
            String fullName,
            User.UserRole role) {
        
        if (userRepository.existsByUsername(username)) {
            log.debug("⚠️  User '{}' đã tồn tại, bỏ qua", username);
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.debug("⚠️  Email '{}' đã tồn tại, bỏ qua", email);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullName(fullName);
        user.addRole(role);

        user = userRepository.save(user);
        log.info("✅ Đã tạo user: {} (Role: {})", username, role);
    }
}
