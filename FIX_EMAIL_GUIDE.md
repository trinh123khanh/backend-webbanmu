# 🔧 Hướng Dẫn Sửa Lỗi Email Không Gửi Được

## ❌ Vấn Đề Hiện Tại

**Email KHÔNG gửi được** khi tạo phiếu giảm giá cá nhân vì:

1. ⚠️ Code gửi email thật đang bị **comment**
2. ⚠️ Email credentials chưa được **cấu hình**
3. ✅ Dependency `spring-boot-starter-mail` **đã có** trong `build.gradle`

---

## 🛠️ Cách Sửa (3 Bước)

### **Bước 1: Uncomment Code Gửi Email Thật**

Mở file: `src/main/java/com/example/backend/service/EmailService.java`

#### 1.1. Uncomment JavaMailSender (dòng 14-15)

**TÌM:**
```java
// Tạm thời comment JavaMailSender để tránh lỗi compile
// private final JavaMailSender mailSender;
```

**THAY BẰNG:**
```java
// JavaMailSender để gửi email thật
private final JavaMailSender mailSender;
```

#### 1.2. Uncomment Code Gửi Email (dòng 48-54)

**TÌM:**
```java
// TODO: Uncomment khi đã cấu hình email đúng
// SimpleMailMessage message = new SimpleMailMessage();
// message.setFrom(fromEmail);
// message.setTo(customerEmail);
// message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
// message.setText(emailContent);
// mailSender.send(message);
```

**THAY BẰNG:**
```java
// Gửi email thật
SimpleMailMessage message = new SimpleMailMessage();
message.setFrom(fromEmail);
message.setTo(customerEmail);
message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
message.setText(emailContent);
mailSender.send(message);
```

#### 1.3. Comment Code Simulation (dòng 56-60)

**TÌM:**
```java
log.info("📧 EMAIL NOTIFICATION (Simulated):");
log.info("   To: {}", customerEmail);
log.info("   Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!");
log.info("   Content: {}", emailContent);
log.info("✅ Email notification logged successfully for: {}", customerEmail);
```

**THAY BẰNG:**
```java
// Code simulation - đã tắt
// log.info("📧 EMAIL NOTIFICATION (Simulated):");
// log.info("   To: {}", customerEmail);
// log.info("   Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!");
// log.info("   Content: {}", emailContent);
// log.info("✅ Email notification logged successfully for: {}", customerEmail);

log.info("✅ Email sent successfully to: {}", customerEmail);
```

#### 1.4. Thêm Import Thiếu

**Ở đầu file, thêm:**
```java
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
```

---

### **Bước 2: Cấu Hình Email Credentials**

#### 2.1. Tạo App Password từ Gmail

1. Vào Google Account: https://myaccount.google.com/
2. Chọn **Security** (Bảo mật)
3. Bật **2-Step Verification** (Xác minh 2 bước) nếu chưa có
4. Tìm **App passwords** (Mật khẩu ứng dụng)
5. Chọn app: **Mail**, device: **Other** → Nhập tên: `TDK Store Backend`
6. Click **Generate** → Copy **16 ký tự** (ví dụ: `abcd efgh ijkl mnop`)

#### 2.2. Cập Nhật application.yml

Mở file: `src/main/resources/application.yml`

**TÌM dòng 52-53:**
```yaml
username: your-email@gmail.com        # ← Nhập email Gmail của bạn ở đây
password: your-16-char-app-password   # ← Nhập App Password (16 ký tự)
```

**THAY BẰNG (ví dụ):**
```yaml
username: tdkstore2024@gmail.com           # Email Gmail thật của bạn
password: abcd efgh ijkl mnop              # App Password vừa tạo (16 ký tự)
```

⚠️ **LƯU Ý**: Không commit email/password lên Git!

#### 2.3. Hoặc Dùng Environment Variables (An toàn hơn)

**Thay bằng:**
```yaml
username: ${MAIL_USERNAME:your-email@gmail.com}
password: ${MAIL_PASSWORD:your-16-char-app-password}
```

**Rồi set biến môi trường:**
```bash
# Windows PowerShell
$env:MAIL_USERNAME="tdkstore2024@gmail.com"
$env:MAIL_PASSWORD="abcd efgh ijkl mnop"

# Linux/Mac
export MAIL_USERNAME="tdkstore2024@gmail.com"
export MAIL_PASSWORD="abcd efgh ijkl mnop"
```

---

### **Bước 3: Build và Restart Server**

```bash
# Dừng server cũ (Ctrl+C)

# Build lại
./gradlew clean build -x test

# Chạy lại server
./gradlew bootRun
```

---

## ✅ Kiểm Tra Email Đã Hoạt Động

### Test 1: Tạo Phiếu Giảm Giá Cá Nhân

1. Vào **Thêm Phiếu Giảm Giá**
2. Chọn **Cá nhân** (không phải Công khai)
3. Chọn **ít nhất 1 khách hàng** có email
4. Click **Thêm mới**

### Test 2: Kiểm Tra Log

Mở file: `logs/application.log`

**Nếu THÀNH CÔNG, sẽ thấy:**
```
✅ Email sent successfully to: customer@example.com
```

**Nếu LỖI, sẽ thấy:**
```
❌ Lỗi khi gửi email thông báo phiếu giảm giá tới customer@example.com: [Chi tiết lỗi]
```

### Test 3: Kiểm Tra Inbox Khách Hàng

Vào email của khách hàng được chọn, kiểm tra:
- **Inbox** hoặc **Spam/Junk**
- Tiêu đề: "🎉 Bạn đã nhận được phiếu giảm giá mới!"
- Nội dung: Thông tin mã phiếu, tên phiếu

---

## 🐛 Troubleshooting

### Lỗi 1: `AuthenticationFailedException`

**Nguyên nhân:** App Password sai hoặc chưa bật 2-Factor Authentication

**Giải pháp:**
1. Kiểm tra App Password đã copy đúng chưa
2. Đảm bảo đã bật 2-Factor Auth trong Google Account
3. Tạo lại App Password mới

### Lỗi 2: `Could not autowire. No beans of 'JavaMailSender' type found`

**Nguyên nhân:** Thiếu import hoặc dependency

**Giải pháp:**
```java
// Thêm import
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
```

### Lỗi 3: Email gửi nhưng không nhận được

**Nguyên nhân:** Email bị vào Spam

**Giải pháp:**
1. Kiểm tra thư mục **Spam/Junk**
2. Mark email as "Not spam"
3. Add `noreply@tdkstore.com` vào contacts

### Lỗi 4: `Connection timeout`

**Nguyên nhân:** Firewall hoặc proxy block port 587

**Giải pháp:**
1. Kiểm tra firewall cho phép port 587
2. Thử đổi port sang 465 (SSL):
```yaml
mail:
  port: 465
  properties:
    mail:
      smtp:
        ssl:
          enable: true
```

---

## 📊 Kiểm Tra Tính Năng Trong Code

### PhieuGiamGiaService.java (dòng ~500-530)

```java
// Gửi email thông báo cho các khách hàng đã chọn
try {
    log.info("Bắt đầu gửi email thông báo cho {} khách hàng", 
             request.getSelectedCustomerIds().size());

    for (Long customerId : request.getSelectedCustomerIds()) {
        Optional<com.example.backend.entity.KhachHang> khachHangOpt = 
            khachHangRepository.findById(customerId);

        if (khachHangOpt.isPresent()) {
            com.example.backend.entity.KhachHang khachHang = khachHangOpt.get();
            if (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty()) {
                emailService.sendPhieuGiamGiaNotification(
                        khachHang.getEmail(),
                        khachHang.getTenKhachHang(),
                        savedPhieuGiamGia.getMaPhieu(),
                        savedPhieuGiamGia.getTenPhieuGiamGia()
                );
                log.info("Đã gửi email thông báo tới khách hàng {} ({})", 
                         khachHang.getTenKhachHang(), khachHang.getEmail());
            }
        }
    }
} catch (Exception emailException) {
    log.error("Lỗi khi gửi email thông báo: {}", emailException.getMessage());
    // Không throw exception để không ảnh hưởng đến logic chính
}
```

---

## 🎯 Tóm Tắt Nhanh

| Bước | Việc Cần Làm | File |
|------|--------------|------|
| 1 | Uncomment `JavaMailSender` | `EmailService.java` dòng 15 |
| 2 | Uncomment code gửi email | `EmailService.java` dòng 48-54 |
| 3 | Comment code simulation | `EmailService.java` dòng 56-60 |
| 4 | Thêm import | `EmailService.java` đầu file |
| 5 | Cấu hình Gmail credentials | `application.yml` dòng 52-53 |
| 6 | Build và restart | Terminal |

---

## ⚠️ Lưu Ý Quan Trọng

1. **KHÔNG commit** email/password lên Git
2. **SỬ DỤNG** environment variables cho production
3. **KIỂM TRA** App Password có đúng 16 ký tự không có khoảng trắng
4. **ENABLE** 2-Factor Authentication trong Google Account
5. **KIỂM TRA** Spam folder nếu không thấy email

---

## 📞 Cần Hỗ Trợ?

Nếu vẫn gặp lỗi:
1. Copy toàn bộ error message từ `logs/application.log`
2. Screenshot cấu hình email trong `application.yml` (che password)
3. Cho biết bước nào bị lỗi


