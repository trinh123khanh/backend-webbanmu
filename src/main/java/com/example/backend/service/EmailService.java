package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    // Tạm thời comment JavaMailSender để tránh lỗi compile
    // private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@tdkstore.com}")
    private String fromEmail;
    
    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Gửi email thông báo nhận phiếu giảm giá cho khách hàng
     */
    @Async
    public void sendPhieuGiamGiaNotification(String customerEmail, String customerName, String phieuCode, String phieuName) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping email notification.");
            return;
        }
        
        try {
            // Tạm thời chỉ log thay vì gửi email thật
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Chúc mừng! Bạn đã nhận được một phiếu giảm giá đặc biệt từ TDK Store.\n\n" +
                "📌 Thông tin phiếu giảm giá:\n" +
                "- Mã phiếu: %s\n" +
                "- Tên phiếu: %s\n\n" +
                "Hãy sử dụng phiếu giảm giá này trong lần mua sắm tiếp theo của bạn!\n\n" +
                "Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                customerName, phieuCode, phieuName
            );
            
            // TODO: Uncomment khi đã cấu hình email đúng
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom(fromEmail);
            // message.setTo(customerEmail);
            // message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
            // message.setText(emailContent);
            // mailSender.send(message);
            
            log.info("📧 EMAIL NOTIFICATION (Simulated):");
            log.info("   To: {}", customerEmail);
            log.info("   Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!");
            log.info("   Content: {}", emailContent);
            log.info("✅ Email notification logged successfully for: {}", customerEmail);
            
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo phiếu giảm giá tới {}: {}", customerEmail, e.getMessage(), e);
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
            String phieuName) {
        
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
                
                sendPhieuGiamGiaNotification(email, name, phieuCode, phieuName);
                successCount++;
                
            } catch (Exception e) {
                log.error("Lỗi khi gửi email tới {}: {}", customerEmails.get(i), e.getMessage());
                failCount++;
            }
        }
        
        log.info("Hoàn thành gửi email: {} thành công, {} thất bại", successCount, failCount);
    }
}

