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
import com.example.backend.repository.OtpTokenRepository;
import com.example.backend.repository.UserRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
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
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã được sử dụng");
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Tạo user mới với role CUSTOMER
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.addRole(User.UserRole.CUSTOMER);

        user = userRepository.save(user);

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
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Xóa các OTP cũ của email này
        otpTokenRepository.deleteByEmail(request.getEmail());

        // Tạo OTP mới
        String otp = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        OtpToken otpToken = new OtpToken();
        otpToken.setToken(otp);
        otpToken.setEmail(request.getEmail());
        otpToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // OTP hết hạn sau 1 giờ
        otpToken.setUsed(false);
        otpToken.setType(OtpToken.OtpType.PASSWORD_RESET);
        otpTokenRepository.save(otpToken);

        // Gửi email OTP
        emailService.sendPasswordResetOtp(
            request.getEmail(),
            user.getFullName() != null ? user.getFullName() : user.getUsername(),
            otp
        );

        log.info("OTP sent to email: {}", request.getEmail());
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

        // Đánh dấu OTP đã được sử dụng (nhưng chưa xóa để tránh lỗi nếu reset password thất bại)
        // otpToken.setUsed(true);
        // otpTokenRepository.save(otpToken);

        log.info("OTP verified for email: {}", request.getEmail());
        
        // Trả về response xác nhận OTP hợp lệ (không cần token ở đây)
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

        // Đánh dấu OTP đã được sử dụng
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        // Xóa các OTP khác của email này (nếu có)
        otpTokenRepository.deleteByEmailAndUsedFalse(request.getEmail());

        log.info("Password reset successfully for email: {}", request.getEmail());

        // Tạo JWT token và trả về để user có thể đăng nhập ngay
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
            .message("Đặt lại mật khẩu thành công")
            .build();
    }
}
