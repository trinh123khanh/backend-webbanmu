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

    /**
     * Gửi email thông báo hóa đơn cho khách hàng
     */
    @Async
    public void sendInvoiceNotification(String customerEmail, String customerName, String maHoaDon,
                                       String trangThai, java.math.BigDecimal tongTien, 
                                       java.math.BigDecimal thanhTien, java.time.LocalDateTime ngayTao,
                                       String diaChiGiaoHang, java.util.List<InvoiceItemInfo> danhSachSanPham) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping invoice notification email.");
            return;
        }

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            log.warn("Email khách hàng trống, không thể gửi thông báo hóa đơn");
            return;
        }

        try {
            // Format ngày tháng
            java.time.format.DateTimeFormatter dateTimeFormatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String ngayTaoText = ngayTao != null ? ngayTao.format(dateTimeFormatter) : "N/A";
            
            // Format tiền
            String tongTienText = String.format("%,.0f VNĐ", tongTien != null ? tongTien.doubleValue() : 0);
            String thanhTienText = String.format("%,.0f VNĐ", thanhTien != null ? thanhTien.doubleValue() : 0);
            
            // Map trạng thái
            String trangThaiText = getStatusLabel(trangThai);
            
            // Tạo danh sách sản phẩm
            StringBuilder sanPhamList = new StringBuilder();
            if (danhSachSanPham != null && !danhSachSanPham.isEmpty()) {
                for (int i = 0; i < danhSachSanPham.size(); i++) {
                    InvoiceItemInfo item = danhSachSanPham.get(i);
                    String itemText = String.format(
                        "%d. %s - Số lượng: %d - Giá: %,.0f VNĐ - Thành tiền: %,.0f VNĐ",
                        i + 1,
                        item.getTenSanPham() != null ? item.getTenSanPham() : "N/A",
                        item.getSoLuong() != null ? item.getSoLuong() : 0,
                        item.getDonGia() != null ? item.getDonGia().doubleValue() : 0,
                        item.getThanhTien() != null ? item.getThanhTien().doubleValue() : 0
                    );
                    sanPhamList.append(itemText).append("\n");
                }
            } else {
                sanPhamList.append("Không có sản phẩm");
            }
            
            // Tạo nội dung email
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Cảm ơn bạn đã đặt hàng tại TDK Store!\n\n" +
                "📋 THÔNG TIN HÓA ĐƠN:\n" +
                "- Mã hóa đơn: %s\n" +
                "- Trạng thái: %s\n" +
                "- Ngày tạo: %s\n" +
                "- Tổng tiền: %s\n" +
                "- Thành tiền: %s\n" +
                "- Địa chỉ giao hàng: %s\n\n" +
                "🛍️ DANH SÁCH SẢN PHẨM:\n%s\n" +
                "Chúng tôi sẽ xử lý đơn hàng của bạn trong thời gian sớm nhất.\n\n" +
                "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                customerName != null ? customerName : "Khách hàng",
                maHoaDon != null ? maHoaDon : "N/A",
                trangThaiText,
                ngayTaoText,
                tongTienText,
                thanhTienText,
                diaChiGiaoHang != null ? diaChiGiaoHang : "N/A",
                sanPhamList.toString()
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customerEmail);
            message.setSubject("📦 Thông báo hóa đơn " + maHoaDon + " - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Invoice notification email sent successfully to: {} (Invoice: {})", customerEmail, maHoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email thông báo hóa đơn tới {}: {}", customerEmail, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến logic chính
        }
    }

    /**
     * Gửi email thông báo thay đổi trạng thái hóa đơn
     */
    @Async
    public void sendInvoiceStatusChangeNotification(String customerEmail, String customerName, 
                                                   String maHoaDon, String oldStatus, String newStatus,
                                                   java.math.BigDecimal thanhTien) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping invoice status change notification.");
            return;
        }

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            log.warn("Email khách hàng trống, không thể gửi thông báo thay đổi trạng thái");
            return;
        }

        try {
            String oldStatusText = getStatusLabel(oldStatus);
            String newStatusText = getStatusLabel(newStatus);
            String thanhTienText = String.format("%,.0f VNĐ", thanhTien != null ? thanhTien.doubleValue() : 0);
            
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Hóa đơn của bạn đã được cập nhật trạng thái.\n\n" +
                "📋 THÔNG TIN HÓA ĐƠN:\n" +
                "- Mã hóa đơn: %s\n" +
                "- Trạng thái cũ: %s\n" +
                "- Trạng thái mới: %s\n" +
                "- Thành tiền: %s\n\n" +
                "%s\n\n" +
                "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                customerName != null ? customerName : "Khách hàng",
                maHoaDon != null ? maHoaDon : "N/A",
                oldStatusText,
                newStatusText,
                thanhTienText,
                getStatusChangeMessage(newStatus)
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customerEmail);
            message.setSubject("🔄 Cập nhật trạng thái hóa đơn " + maHoaDon + " - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Invoice status change notification sent successfully to: {} (Invoice: {}, Status: {} -> {})", 
                customerEmail, maHoaDon, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email thông báo thay đổi trạng thái tới {}: {}", customerEmail, e.getMessage(), e);
        }
    }

    private String getStatusLabel(String status) {
        if (status == null) return "N/A";
        switch (status) {
            case "CHO_XAC_NHAN": return "Chờ xác nhận";
            case "DA_XAC_NHAN": return "Đã xác nhận - Chờ vận chuyển";
            case "DANG_GIAO_HANG": return "Đang giao hàng";
            case "DA_GIAO_HANG": return "Đã giao hàng";
            case "DA_HUY": case "HUY": return "Đã hủy";
            default: return status;
        }
    }

    private String getStatusChangeMessage(String newStatus) {
        if (newStatus == null) return "";
        switch (newStatus) {
            case "DA_XAC_NHAN":
                return "Đơn hàng của bạn đã được xác nhận và đang được chuẩn bị để giao hàng. Chúng tôi sẽ thông báo khi đơn hàng được gửi đi.";
            case "DANG_GIAO_HANG":
                return "Đơn hàng của bạn đang được vận chuyển. Vui lòng chuẩn bị nhận hàng.";
            case "DA_GIAO_HANG":
                return "Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã mua sắm tại TDK Store!";
            case "DA_HUY": case "HUY":
                return "Đơn hàng của bạn đã bị hủy. Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi.";
            default:
                return "Trạng thái đơn hàng đã được cập nhật.";
        }
    }

    /**
     * Inner class để chứa thông tin sản phẩm trong hóa đơn
     */
    public static class InvoiceItemInfo {
        private String tenSanPham;
        private Integer soLuong;
        private java.math.BigDecimal donGia;
        private java.math.BigDecimal thanhTien;

        public InvoiceItemInfo() {}

        public InvoiceItemInfo(String tenSanPham, Integer soLuong, java.math.BigDecimal donGia, java.math.BigDecimal thanhTien) {
            this.tenSanPham = tenSanPham;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
        }

        public String getTenSanPham() { return tenSanPham; }
        public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
        public Integer getSoLuong() { return soLuong; }
        public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
        public java.math.BigDecimal getDonGia() { return donGia; }
        public void setDonGia(java.math.BigDecimal donGia) { this.donGia = donGia; }
        public java.math.BigDecimal getThanhTien() { return thanhTien; }
        public void setThanhTien(java.math.BigDecimal thanhTien) { this.thanhTien = thanhTien; }
    }
}
