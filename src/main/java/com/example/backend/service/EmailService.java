package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // JavaMailSender để gửi email thật
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@tdkstore.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    /**
     * Gửi email thông báo nhận phiếu giảm giá cho khách hàng
     */
    @Async
    public void sendPhieuGiamGiaNotification(String customerEmail, String customerName, String phieuCode, String phieuName,
                                               java.time.LocalDate ngayBatDau, java.time.LocalDate ngayKetThuc,
                                               java.math.BigDecimal giaTriGiam, Boolean loaiPhieuGiamGia,
                                               java.math.BigDecimal hoaDonToiThieu) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping email notification.");
            return;
        }

        try {
            // Format giá trị giảm
            String giaTriGiamText;
            if (loaiPhieuGiamGia != null && loaiPhieuGiamGia) {
                // Tiền mặt
                giaTriGiamText = String.format("%,.0f VNĐ", giaTriGiam != null ? giaTriGiam.doubleValue() : 0);
            } else {
                // Phần trăm
                giaTriGiamText = String.format("%s%%", giaTriGiam != null ? giaTriGiam.toString() : "0");
            }
            
            // Format hóa đơn tối thiểu
            String hoaDonToiThieuText = String.format("%,.0f VNĐ", hoaDonToiThieu != null ? hoaDonToiThieu.doubleValue() : 0);
            
            // Format ngày tháng
            java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String ngayBatDauText = ngayBatDau != null ? ngayBatDau.format(dateFormatter) : "N/A";
            String ngayKetThucText = ngayKetThuc != null ? ngayKetThuc.format(dateFormatter) : "N/A";
            
            // Tạo nội dung email
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Chúc mừng! Bạn đã nhận được một phiếu giảm giá đặc biệt từ TDK Store.\n\n" +
                "📌 Thông tin phiếu giảm giá:\n" +
                "- Mã phiếu: %s\n" +
                "- Tên phiếu: %s\n" +
                "- Giá trị giảm: %s\n" +
                "- Hóa đơn tối thiểu: %s\n" +
                "- Ngày bắt đầu: %s\n" +
                "- Ngày kết thúc: %s\n\n" +
                "Hãy sử dụng phiếu giảm giá này trong lần mua sắm tiếp theo của bạn!\n\n" +
                "Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                customerName, phieuCode, phieuName, giaTriGiamText, hoaDonToiThieuText, ngayBatDauText, ngayKetThucText
            );

            // Gửi email thật
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customerEmail);
            message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Email sent successfully to: {} (Phiếu: {})", customerEmail, phieuCode);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email thông báo phiếu giảm giá tới {}: {}", customerEmail, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến logic chính
        }
    }

    /**
     * Gửi email cho nhiều khách hàng cùng lúc
     */
    @Async
    public void sendPhieuGiamGiaNotificationToMultipleCustomers(
            java.util.List<String> customerEmails,
            java.util.List<String> customerNames,
            String phieuCode,
            String phieuName,
            java.time.LocalDate ngayBatDau,
            java.time.LocalDate ngayKetThuc,
            java.math.BigDecimal giaTriGiam,
            Boolean loaiPhieuGiamGia,
            java.math.BigDecimal hoaDonToiThieu) {

        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping bulk email notification.");
            return;
        }

        if (customerEmails == null || customerEmails.isEmpty()) {
            log.warn("Danh sách email khách hàng trống");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < customerEmails.size(); i++) {
            try {
                String email = customerEmails.get(i);
                String name = i < customerNames.size() ? customerNames.get(i) : "Khách hàng";

                sendPhieuGiamGiaNotification(email, name, phieuCode, phieuName, 
                                            ngayBatDau, ngayKetThuc, giaTriGiam, loaiPhieuGiamGia, hoaDonToiThieu);
                successCount++;

            } catch (Exception e) {
                log.error("Lỗi khi gửi email tới {}: {}", customerEmails.get(i), e.getMessage());
                failCount++;
            }
        }

        log.info("Hoàn thành gửi email: {} thành công, {} thất bại", successCount, failCount);
    }

    /**
     * Gửi email OTP cho đặt lại mật khẩu
     */
    @Async
    public void sendPasswordResetOtp(String email, String name, String otp) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping password reset email.");
            return;
        }

        try {
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
                "Mã OTP của bạn là: %s\n\n" +
                "Mã OTP này sẽ hết hạn sau 1 giờ.\n\n" +
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                name != null ? name : "Khách hàng",
                otp
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("🔐 Đặt lại mật khẩu - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Password reset OTP sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email OTP đặt lại mật khẩu tới {}: {}", email, e.getMessage(), e);
        }
    }
}
