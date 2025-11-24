package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

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

    /**
     * Gửi email thông tin tài khoản đăng nhập cho nhân viên mới
     */
    @Async
    public void sendEmployeeAccountInfo(String email, String employeeName, String username, String password, String maNhanVien) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping employee account info email.");
            return;
        }

        try {
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Chúc mừng! Bạn đã được thêm vào hệ thống TDK Store với vai trò nhân viên.\n\n" +
                "📌 Thông tin tài khoản đăng nhập:\n" +
                "- Mã nhân viên: %s\n" +
                "- Tên đăng nhập: %s\n" +
                "- Mật khẩu: %s\n\n" +
                "⚠️ LƯU Ý QUAN TRỌNG:\n" +
                "- Vui lòng đổi mật khẩu ngay sau lần đăng nhập đầu tiên để bảo mật tài khoản.\n" +
                "- Không chia sẻ thông tin đăng nhập với người khác.\n" +
                "- Nếu bạn không yêu cầu tài khoản này, vui lòng liên hệ quản trị viên.\n\n" +
                "Bạn có thể đăng nhập vào hệ thống tại: http://localhost:4200/login\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                employeeName != null ? employeeName : "Nhân viên",
                maNhanVien != null ? maNhanVien : "N/A",
                username,
                password
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("🔑 Thông tin tài khoản đăng nhập - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Employee account info sent successfully to: {} (Username: {})", email, username);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email thông tin tài khoản tới {}: {}", email, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến logic tạo nhân viên
        }
    }

    /**
     * Gửi email báo cáo thống kê với nội dung HTML
     */
    @Async
    public void sendStatisticsReport(String toEmail, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping statistics report email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content
            
            mailSender.send(message);
            
            log.info("✅ Statistics report email sent successfully to: {} (Subject: {})", toEmail, subject);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email báo cáo thống kê tới {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
