package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // OPTIONS requests for CORS preflight - phải cho phép trước
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // User info endpoint - yêu cầu authentication (đặt trước rule chung)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auth/me").authenticated()
                // Public endpoints - không cần authentication (đặt trước để ưu tiên)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/customer/products/**").permitAll()
                // TẠM THỜI mở cho phép tạo/cập nhật biến thể để tránh 403 trong lúc phát triển UI
                .requestMatchers("/api/chi-tiet-san-pham/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // Statistics endpoints - cho phép public GET để hiển thị best-selling products trên shop
                // Đặt rule cụ thể TRƯỚC rule chung để đảm bảo ưu tiên
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/statistics/best-selling-products").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/statistics/best-selling-products/**").permitAll()
                // Statistics detailed endpoints - cho phép public GET để hiển thị báo cáo thống kê
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/statistics/detailed").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/statistics/detailed/**").permitAll()
                // Statistics endpoints khác - ADMIN và STAFF
                .requestMatchers("/api/statistics/**").hasAnyRole("ADMIN", "STAFF")
                
                // Report endpoints - cho phép ADMIN và STAFF gửi báo cáo
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/api/report-test/**").hasAnyRole("ADMIN", "STAFF")
                
                // Admin endpoints - chỉ ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Staff endpoints - ADMIN và STAFF
                .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF")
                
                // Product & variant management - cho phép ADMIN và STAFF
                .requestMatchers("/api/san-pham/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/api/admin/products/**").hasAnyRole("ADMIN", "STAFF")
                
                // Customer endpoints - CUSTOMER, hoặc public nếu cần
                .requestMatchers("/api/customer/profile/**").hasRole("CUSTOMER")
                .requestMatchers("/api/customer/orders/**").hasRole("CUSTOMER")
                

                // Khách hàng lấy thông tin cá nhân từ JWT token - yêu cầu authentication
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/khach-hang/me").authenticated()
                

                // CHO PHÉP tạo hóa đơn mà không cần authentication (để khách hàng có thể đặt hàng)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/hoa-don").permitAll()
                
                // CHO PHÉP GET hóa đơn để xem chi tiết (cho customer và public access)
                // Cho phép tất cả GET requests đến /api/hoa-don/** (bao gồm /api/hoa-don/{id}, /api/hoa-don/page, etc.)
                // Đặt TRƯỚC rule chung để ưu tiên
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/hoa-don/**").permitAll()
                
                // CHO PHÉP truy cập giỏ hàng chờ (hoa-don-cho) để checkout
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/hoa-don-cho/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/hoa-don-cho/**").permitAll()
                
                // Lịch sử thay đổi hóa đơn - yêu cầu ADMIN hoặc STAFF
                .requestMatchers("/api/hoa-don-activity/**").hasAnyRole("ADMIN", "STAFF")
                
                // Backward compatibility - endpoints cũ cho phép ADMIN và STAFF (PUT, PATCH, DELETE)
                // GET và POST đã được cho phép ở trên, chỉ cần bảo vệ các method khác
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                // Fallback: các method khác của /api/hoa-don/** yêu cầu ADMIN hoặc STAFF
                .requestMatchers("/api/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                
                // TẠM THỜI: Mở toàn bộ API để debug lỗi 403 khi tạo sản phẩm
                .anyRequest().permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
