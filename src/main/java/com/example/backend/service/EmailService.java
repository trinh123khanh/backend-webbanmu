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
        // Gọi overload method với phuongThucThanhToan = null (mặc định)
        sendInvoiceStatusChangeNotification(customerEmail, customerName, maHoaDon, oldStatus, newStatus, thanhTien, null);
    }

    /**
     * Gửi email thông báo thay đổi trạng thái hóa đơn (với phương thức thanh toán)
     */
    @Async
    public void sendInvoiceStatusChangeNotification(String customerEmail, String customerName, 
                                                   String maHoaDon, String oldStatus, String newStatus,
                                                   java.math.BigDecimal thanhTien, String phuongThucThanhToan) {
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
            
            // ✅ QUAN TRỌNG: Xác định phương thức thanh toán để hiển thị message phù hợp
            boolean isTransferPayment = false;
            if (phuongThucThanhToan != null && !phuongThucThanhToan.trim().isEmpty()) {
                // Normalize: trim và loại bỏ khoảng trắng thừa
                String phuongThuc = phuongThucThanhToan.trim().replaceAll("\\s+", " ");
                String phuongThucLower = phuongThuc.toLowerCase();
                
                // Log để debug
                log.info("🔍 Checking payment method: '{}' (normalized: '{}', lower: '{}')", 
                    phuongThucThanhToan, phuongThuc, phuongThucLower);
                
                // Kiểm tra nếu là chuyển khoản - kiểm tra nhiều cách viết khác nhau
                // Bao gồm: có dấu, không dấu, chữ hoa, chữ thường, có khoảng trắng
                isTransferPayment = 
                    // Kiểm tra với dấu tiếng Việt
                    phuongThucLower.contains("chuyển khoản") || 
                    phuongThucLower.contains("chuyểnkhoản") ||
                    phuongThucLower.equals("chuyển khoản") ||
                    // Kiểm tra không dấu
                    phuongThucLower.contains("chuyen khoan") || 
                    phuongThucLower.contains("chuyenkhoan") ||
                    phuongThucLower.equals("chuyen khoan") ||
                    // Kiểm tra tiếng Anh
                    phuongThucLower.equals("transfer") ||
                    phuongThucLower.contains("transfer") ||
                    // Kiểm tra trực tiếp với chữ hoa (trường hợp đặc biệt)
                    phuongThuc.equals("Chuyển khoản") ||
                    phuongThuc.equals("Chuyển Khoản") ||
                    phuongThuc.equals("CHUYỂN KHOẢN") ||
                    phuongThuc.equals("CHUYEN KHOAN");
                
                log.info("💰 Payment method check result: isTransferPayment = {} (method: '{}', status: {})", 
                    isTransferPayment, phuongThuc, newStatus);
            } else {
                log.warn("⚠️ phuongThucThanhToan is null or empty, defaulting to cash payment message (status: {})", newStatus);
            }
            
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
                getStatusChangeMessage(newStatus, isTransferPayment)
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customerEmail);
            message.setSubject("🔄 Cập nhật trạng thái hóa đơn " + maHoaDon + " - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Invoice status change notification sent successfully to: {} (Invoice: {}, Status: {} -> {}, Payment: {})", 
                customerEmail, maHoaDon, oldStatus, newStatus, phuongThucThanhToan != null ? phuongThucThanhToan : "N/A");

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
        // Gọi overload method với isTransferPayment = false (mặc định)
        return getStatusChangeMessage(newStatus, false);
    }

    private String getStatusChangeMessage(String newStatus, boolean isTransferPayment) {
        if (newStatus == null) return "";
        switch (newStatus) {
            case "DA_XAC_NHAN":
                return "Đơn hàng của bạn đã được xác nhận và đang được chuẩn bị để giao hàng. Chúng tôi sẽ thông báo khi đơn hàng được gửi đi.";
            case "DANG_GIAO_HANG":
                return "Đơn hàng của bạn đang được vận chuyển. Vui lòng chuẩn bị nhận hàng.";
            case "DA_GIAO_HANG":
                return "Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã mua sắm tại TDK Store!";
            case "DA_HUY": case "HUY":
                // ✅ QUAN TRỌNG: Nếu là đơn hàng chuyển khoản, hiển thị message yêu cầu trao đổi thông tin
                // Nếu là đơn hàng tiền mặt, giữ nguyên message cũ
                if (isTransferPayment) {
                    return "Đơn hàng của bạn đã bị hủy, để nhận được tiền hoàn phí thanh toán, quý khách vui lòng trao đổi thông tin với shop qua email này hoặc trao đổi trực tiếp với shop qua message, TDK xin cảm ơn.";
                } else {
                    return "Đơn hàng của bạn đã bị hủy. Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi.";
                }
            default:
                return "Trạng thái đơn hàng đã được cập nhật.";
        }
    }

    /**
     * Gửi email yêu cầu thông tin hoàn tiền khi hủy đơn hàng đã thanh toán
     */
    @Async
    public void sendRefundRequestEmail(String customerEmail, String customerName, String maHoaDon,
                                      java.math.BigDecimal thanhTien, String refundLink) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping refund request email.");
            return;
        }

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            log.warn("Email khách hàng trống, không thể gửi email yêu cầu hoàn tiền");
            return;
        }

        try {
            String thanhTienText = String.format("%,.0f VNĐ", thanhTien != null ? thanhTien.doubleValue() : 0);
            
            // Tạo nội dung email với link để khách hàng nhập thông tin
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Chúng tôi rất tiếc vì đơn hàng của bạn đã bị hủy.\n\n" +
                "📋 THÔNG TIN HÓA ĐƠN:\n" +
                "- Mã hóa đơn: %s\n" +
                "- Số tiền cần hoàn: %s\n\n" +
                "💰 YÊU CẦU HOÀN TIỀN:\n" +
                "Để chúng tôi có thể hoàn tiền cho bạn, vui lòng cung cấp thông tin tài khoản ngân hàng của bạn bằng cách:\n\n" +
                "1. Truy cập link sau: %s\n" +
                "2. Nhập mã hóa đơn: %s\n" +
                "3. Điền thông tin tài khoản ngân hàng:\n" +
                "   - Số tài khoản\n" +
                "   - Tên ngân hàng\n" +
                "   - Tên chủ tài khoản\n\n" +
                "⚠️ LƯU Ý:\n" +
                "- Thông tin tài khoản sẽ được bảo mật và chỉ sử dụng để hoàn tiền\n" +
                "- Tiền sẽ được hoàn trả trong vòng 3-5 ngày làm việc sau khi nhận được thông tin\n" +
                "- Nếu bạn không cung cấp thông tin trong vòng 7 ngày, vui lòng liên hệ trực tiếp với chúng tôi\n\n" +
                "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi:\n" +
                "- Email: support@tdkstore.com\n" +
                "- Hotline: 0909 123 456\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                customerName != null ? customerName : "Khách hàng",
                maHoaDon != null ? maHoaDon : "N/A",
                thanhTienText,
                refundLink != null ? refundLink : "http://localhost:4200/refund",
                maHoaDon != null ? maHoaDon : "N/A"
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customerEmail);
            message.setSubject("💰 Yêu cầu thông tin hoàn tiền - Hóa đơn " + maHoaDon + " - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Refund request email sent successfully to: {} (Invoice: {})", customerEmail, maHoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email yêu cầu hoàn tiền tới {}: {}", customerEmail, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến logic chính
        }
    }

    /**
     * Gửi email thông báo cập nhật địa chỉ giao hàng cho khách hàng
     */
    @Async
    public void sendAddressUpdateEmail(String customerEmail, String customerName, String maHoaDon,
                                      String oldAddress, String newAddress) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping address update email.");
            return;
        }

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            log.warn("Email khách hàng trống, không thể gửi thông báo cập nhật địa chỉ");
            return;
        }

        try {
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Chúng tôi xin thông báo rằng địa chỉ giao hàng của đơn hàng của bạn đã được cập nhật.\n\n" +
                "📋 THÔNG TIN HÓA ĐƠN:\n" +
                "- Mã hóa đơn: %s\n\n" +
                "📍 THAY ĐỔI ĐỊA CHỈ:\n" +
                "- Địa chỉ cũ: %s\n" +
                "- Địa chỉ mới: %s\n\n" +
                "⚠️ LƯU Ý:\n" +
                "- Nếu địa chỉ mới khác với địa chỉ bạn đã cung cấp, vui lòng liên hệ với chúng tôi ngay.\n" +
                "- Phí giao hàng có thể thay đổi tùy theo địa chỉ mới.\n" +
                "- Đơn hàng sẽ được giao đến địa chỉ mới này.\n\n" +
                "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi:\n" +
                "- Email: support@tdkstore.com\n" +
                "- Hotline: 0909 123 456\n\n" +
                "Trân trọng,\n" +
                "TDK Store - Bán mũ bảo hiểm",
                customerName != null ? customerName : "Khách hàng",
                maHoaDon != null ? maHoaDon : "N/A",
                oldAddress != null && !oldAddress.trim().isEmpty() ? oldAddress : "Chưa có địa chỉ",
                newAddress != null && !newAddress.trim().isEmpty() ? newAddress : "Chưa có địa chỉ"
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customerEmail);
            message.setSubject("📍 Cập nhật địa chỉ giao hàng - Hóa đơn " + maHoaDon + " - TDK Store");
            message.setText(emailContent);
            mailSender.send(message);

            log.info("✅ Address update email sent successfully to: {} (Invoice: {})", customerEmail, maHoaDon);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email thông báo cập nhật địa chỉ tới {}: {}", customerEmail, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến logic chính
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
