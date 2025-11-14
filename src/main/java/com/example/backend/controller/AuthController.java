package com.example.backend.controller;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.ForgotPasswordRequest;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.ResetPasswordRequest;
import com.example.backend.dto.UserInfoDTO;
import com.example.backend.dto.VerifyOtpRequest;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Register error", e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok(AuthResponse.builder()
                .message("Mã OTP đã được gửi đến email của bạn")
                .build());
        } catch (Exception e) {
            log.error("Forgot password error", e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            AuthResponse response = authService.verifyOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Verify OTP error", e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            AuthResponse response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Reset password error", e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * Lấy thông tin user hiện tại (username và roles)
     * Endpoint: GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Lấy username từ JWT token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
                log.warn("⚠️ Unauthorized access to /api/auth/me endpoint");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("message", "Bạn cần đăng nhập để xem thông tin tài khoản");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            String username = auth.getName();
            log.info("📋 [AuthController] Lấy thông tin user hiện tại từ username: {}", username);
            
            // Tìm User từ username
            var userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                log.error("❌ [AuthController] Không tìm thấy user với username: {}", username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("message", "Không tìm thấy tài khoản với username: " + username);
                errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            User user = userOptional.get();
            log.info("✅ [AuthController] Tìm thấy user: {} (ID: {})", username, user.getId());
            
            // Chuyển đổi roles thành RoleInfo list
            List<UserInfoDTO.RoleInfo> roleInfoList = new ArrayList<>();
            for (User.UserRole role : user.getRoles()) {
                String displayName = getRoleDisplayName(role);
                roleInfoList.add(UserInfoDTO.RoleInfo.builder()
                    .roleName(role.name())
                    .roleDisplayName(displayName)
                    .build());
            }
            
            // Tạo UserInfoDTO
            UserInfoDTO userInfo = UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roleInfoList)
                .build();
            
            log.info("✅ [AuthController] Trả về thông tin user: username={}, roles={}", 
                userInfo.getUsername(), userInfo.getRoles());
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("❌ [AuthController] Lỗi khi lấy thông tin user hiện tại", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Có lỗi xảy ra khi lấy thông tin tài khoản: " + e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Chuyển đổi role enum sang tên hiển thị tiếng Việt
     */
    private String getRoleDisplayName(User.UserRole role) {
        switch (role) {
            case ADMIN:
                return "Quản trị viên";
            case STAFF:
                return "Nhân viên";
            case CUSTOMER:
                return "Khách hàng";
            default:
                return role.name();
        }
    }
}
