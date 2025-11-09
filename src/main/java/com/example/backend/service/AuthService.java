package com.example.backend.service;

import com.example.backend.config.JwtUtil;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.ForgotPasswordRequest;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.ResetPasswordRequest;
import com.example.backend.dto.VerifyOtpRequest;
import com.example.backend.entity.OtpToken;
import com.example.backend.entity.User;
import com.example.backend.entity.KhachHang;
import com.example.backend.repository.OtpTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Xác thực user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            Set<String> roles = user.getRoles().stream()
                .map(role -> role.name())
                .collect(Collectors.toSet());

            String token = jwtUtil.generateToken(userDetails, roles);

            return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.getUsername(), e);
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng", e);
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("🔐 Bắt đầu đăng ký user: {}", request.getUsername());
        
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("⚠️ Username đã tồn tại: {}", request.getUsername());
            throw new RuntimeException("Tên đăng nhập đã được sử dụng");
        }

        // Kiểm tra email đã tồn tại trong User
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("⚠️ Email đã tồn tại trong User: {}", request.getEmail());
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Kiểm tra email đã tồn tại trong KhachHang chưa (tránh duplicate)
        if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("⚠️ Email đã tồn tại trong KhachHang: {}", request.getEmail());
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Tạo user mới với role CUSTOMER
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.addRole(User.UserRole.CUSTOMER);

        try {
            user = userRepository.save(user);
            log.info("✅ Đã tạo User thành công: {} (ID: {})", user.getUsername(), user.getId());
        } catch (Exception ex) {
            log.error("❌ Không thể tạo User: {}", ex.getMessage(), ex);
            throw new RuntimeException("Không thể tạo tài khoản: " + ex.getMessage(), ex);
        }

        // Tạo bản ghi KhachHang liên kết với user vừa tạo
        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(request.getFullName() != null && !request.getFullName().isBlank()
                ? request.getFullName() : request.getUsername());
        kh.setEmail(request.getEmail());
        kh.setSoDienThoai(null);
        kh.setTrangThai(true);
        kh.setNgayTao(java.time.LocalDate.now());
        kh.setUser(user); // liên kết user_id
        
        // Set các giá trị mặc định
        kh.setSoLanMua(0);
        kh.setDiemTichLuy(0);
        // lanMuaGanNhat có thể null
        
        // Tạo mã khách hàng đơn giản, duy nhất - đảm bảo không trùng
        String mkh;
        int attempts = 0;
        do {
            mkh = "KH" + System.currentTimeMillis() + (attempts > 0 ? "_" + attempts : "");
            attempts++;
            if (attempts > 10) {
                // Nếu sau 10 lần thử vẫn trùng, thêm random để đảm bảo unique
                mkh = "KH" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
                break;
            }
        } while (khachHangRepository.existsByMaKhachHang(mkh));
        
        kh.setMaKhachHang(mkh);
        log.info("📝 Đã tạo mã khách hàng: {}", mkh);
        
        // Save KhachHang - QUAN TRỌNG: Phải save trong cùng transaction
        try {
            kh = khachHangRepository.saveAndFlush(kh);
            log.info("✅ Đã tạo KhachHang thành công: {} (ID: {}, maKhachHang: {})", 
                    kh.getTenKhachHang(), kh.getId(), kh.getMaKhachHang());
        } catch (Exception ex) {
            log.error("❌ Không thể tạo bản ghi KhachHang cho user {}: {}", user.getUsername(), ex.getMessage(), ex);
            // Rollback transaction bằng cách throw exception
            // Transaction sẽ tự động rollback khi có exception
            throw new RuntimeException("Không thể tạo thông tin khách hàng: " + ex.getMessage(), ex);
        }

        // Tạo JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Set<String> roles = user.getRoles().stream()
            .map(role -> role.name())
            .collect(Collectors.toSet());
        String token = jwtUtil.generateToken(userDetails, roles);

        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .roles(roles)
            .message("Đăng ký thành công")
            .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        // Xóa các OTP cũ của email này
        otpTokenRepository.deleteByEmail(request.getEmail());

        // Tạo OTP ngẫu nhiên (8 ký tự chữ số/hex, in hoa) để đảm bảo uniqueness
        String otp = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        OtpToken otpToken = new OtpToken();
        otpToken.setToken(otp);
        otpToken.setEmail(request.getEmail());
        otpToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // OTP hết hạn sau 1 giờ
        otpToken.setUsed(false);
        otpToken.setType(OtpToken.OtpType.PASSWORD_RESET);
        otpTokenRepository.save(otpToken);

        // Gửi email OTP
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                emailService.sendPasswordResetOtp(
                    request.getEmail(),
                    user.getFullName() != null ? user.getFullName() : user.getUsername(),
                    otp
                );
            } catch (Exception ex) {
                log.warn("Failed to send OTP email: {}", ex.getMessage());
            }
        } else {
            log.warn("Forgot password requested for non-existing email: {}. Returning OK without sending mail.", request.getEmail());
        }

        log.info("OTP generated and stored for email: {}", request.getEmail());
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        // Tìm OTP token hợp lệ
        OtpToken otpToken = otpTokenRepository.findByEmailAndTokenAndType(
            request.getEmail(),
            request.getOtp(),
            OtpToken.OtpType.PASSWORD_RESET
        ).orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Kiểm tra OTP đã được sử dụng chưa
        if (otpToken.isUsed()) {
            throw new RuntimeException("Mã OTP đã được sử dụng");
        }

        // Kiểm tra OTP còn hạn không
        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        return AuthResponse.builder()
            .message("Mã OTP hợp lệ. Vui lòng nhập mật khẩu mới.")
            .build();
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Tìm OTP token hợp lệ
        OtpToken otpToken = otpTokenRepository.findByEmailAndTokenAndType(
            request.getEmail(),
            request.getOtp(),
            OtpToken.OtpType.PASSWORD_RESET
        ).orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Kiểm tra OTP đã được sử dụng chưa
        if (otpToken.isUsed()) {
            throw new RuntimeException("Mã OTP đã được sử dụng");
        }

        // Kiểm tra OTP còn hạn không
        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Đánh dấu OTP đã được sử dụng và xóa các OTP khác chưa dùng
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        otpTokenRepository.deleteByEmailAndUsedFalse(request.getEmail());

        return AuthResponse.builder()
            .message("Đặt lại mật khẩu thành công")
            .build();
    }
}
