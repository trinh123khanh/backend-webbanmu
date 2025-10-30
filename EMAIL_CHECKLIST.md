# ✅ Email Configuration Checklist

## 📋 Danh Sách Kiểm Tra

### ☐ **1. Kiểm Tra Dependency**
```bash
# Mở build.gradle, tìm dòng này:
implementation 'org.springframework.boot:spring-boot-starter-mail'
```
✅ ĐÃ CÓ - Không cần thay đổi

---

### ☐ **2. Tạo App Password Gmail**

1. ☐ Vào https://myaccount.google.com/security
2. ☐ Bật **2-Step Verification** (nếu chưa có)
3. ☐ Tạo **App Password**:
   - App: Mail
   - Device: Other (TDK Store Backend)
4. ☐ Copy **16 ký tự** (ví dụ: `abcd efgh ijkl mnop`)

---

### ☐ **3. Sửa EmailService.java**

File: `src/main/java/com/example/backend/service/EmailService.java`

#### ☐ 3.1. Thêm imports (đầu file, sau package)
```java
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
```

#### ☐ 3.2. Uncomment JavaMailSender (dòng 14-15)
**TRƯỚC:**
```java
// Tạm thời comment JavaMailSender để tránh lỗi compile
// private final JavaMailSender mailSender;
```

**SAU:**
```java
// JavaMailSender để gửi email thật
private final JavaMailSender mailSender;
```

#### ☐ 3.3. Uncomment code gửi email (dòng 48-54)
**TRƯỚC:**
```java
// TODO: Uncomment khi đã cấu hình email đúng
// SimpleMailMessage message = new SimpleMailMessage();
// message.setFrom(fromEmail);
// message.setTo(customerEmail);
// message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
// message.setText(emailContent);
// mailSender.send(message);
```

**SAU:**
```java
// Gửi email thật
SimpleMailMessage message = new SimpleMailMessage();
message.setFrom(fromEmail);
message.setTo(customerEmail);
message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
message.setText(emailContent);
mailSender.send(message);
```

#### ☐ 3.4. Comment simulation logs (dòng 56-60)
**TRƯỚC:**
```java
log.info("📧 EMAIL NOTIFICATION (Simulated):");
log.info("   To: {}", customerEmail);
log.info("   Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!");
log.info("   Content: {}", emailContent);
log.info("✅ Email notification logged successfully for: {}", customerEmail);
```

**SAU:**
```java
// Simulation disabled
// log.info("📧 EMAIL NOTIFICATION (Simulated):");
// log.info("   To: {}", customerEmail);
// log.info("   Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!");
// log.info("   Content: {}", emailContent);
// log.info("✅ Email notification logged successfully for: {}", customerEmail);

log.info("✅ Email sent successfully to: {}", customerEmail);
```

---

### ☐ **4. Cấu Hình application.yml**

File: `src/main/resources/application.yml`

#### ☐ 4.1. Tìm dòng 52-53
**TRƯỚC:**
```yaml
username: your-email@gmail.com        # ← Nhập email Gmail của bạn ở đây
password: your-16-char-app-password   # ← Nhập App Password (16 ký tự)
```

#### ☐ 4.2. Thay bằng email thật
**SAU:**
```yaml
username: tdkstore2024@gmail.com      # Email Gmail của bạn
password: abcd efgh ijkl mnop         # App Password 16 ký tự (không có khoảng trắng)
```

⚠️ **Hoặc dùng environment variables (khuyến nghị):**
```yaml
username: ${MAIL_USERNAME:your-email@gmail.com}
password: ${MAIL_PASSWORD:your-16-char-app-password}
```

Rồi set trong terminal:
```bash
# PowerShell
$env:MAIL_USERNAME="tdkstore2024@gmail.com"
$env:MAIL_PASSWORD="abcdefghijklmnop"
```

---

### ☐ **5. Build và Restart**

```bash
# 1. Dừng server (Ctrl+C)

# 2. Clean build
./gradlew clean build -x test

# 3. Start server
./gradlew bootRun
```

---

### ☐ **6. Test Email**

#### ☐ 6.1. Tạo phiếu giảm giá cá nhân
1. ☐ Vào **Quản Lý Giảm Giá** → **Phiếu Giảm Giá** → **Thêm mới**
2. ☐ Chọn **Cá nhân** (không phải Công khai)
3. ☐ Chọn **ít nhất 1 khách hàng** có email
4. ☐ Click **Thêm mới**

#### ☐ 6.2. Kiểm tra logs
```bash
# Mở file
tail -f logs/application.log
```

**Tìm dòng:**
```
Bắt đầu gửi email thông báo cho X khách hàng
Đã gửi email thông báo tới khách hàng [Tên] ([Email])
✅ Email sent successfully to: customer@example.com
Hoàn thành gửi email thông báo
```

#### ☐ 6.3. Kiểm tra inbox
- ☐ Kiểm tra **Inbox** của khách hàng
- ☐ Nếu không có, kiểm tra **Spam/Junk**
- ☐ Email subject: "🎉 Bạn đã nhận được phiếu giảm giá mới!"

---

## 🐛 Troubleshooting

### ❌ Lỗi: AuthenticationFailedException

**Nguyên nhân:**
- App Password sai
- Chưa bật 2-Factor Auth

**Giải pháp:**
- ☐ Kiểm tra lại App Password (16 ký tự, không có khoảng trắng)
- ☐ Tạo lại App Password mới
- ☐ Bật 2-Factor Authentication

### ❌ Lỗi: Could not autowire JavaMailSender

**Nguyên nhân:**
- Thiếu import

**Giải pháp:**
- ☐ Thêm import:
  ```java
  import org.springframework.mail.javamail.JavaMailSender;
  import org.springframework.mail.SimpleMailMessage;
  ```

### ❌ Email không nhận được

**Nguyên nhân:**
- Email vào Spam

**Giải pháp:**
- ☐ Kiểm tra thư mục Spam/Junk
- ☐ Mark as "Not spam"
- ☐ Add vào contacts

---

## 📊 Status Check

### Kiểm tra trạng thái hiện tại:

```bash
# 1. Xem logs
cat logs/application.log | grep "EMAIL"

# 2. Nếu thấy "Simulated" → Chưa enable
# 3. Nếu thấy "sent successfully" → Đã enable
```

---

## 🎯 Quick Summary

| Mục | Status | Action |
|-----|--------|--------|
| Dependency | ✅ OK | Không cần làm gì |
| Gmail App Password | ❓ | Tạo tại Google Account |
| EmailService.java | ❌ Commented | Uncomment code |
| application.yml | ❌ Placeholder | Điền email thật |
| Build & Restart | ❓ | `./gradlew bootRun` |
| Test | ❓ | Tạo phiếu cá nhân |

---

## 📞 Cần Hỗ Trợ?

Nếu vẫn gặp lỗi, cung cấp:
1. Screenshot error trong `logs/application.log`
2. Đã làm đến bước nào trong checklist
3. Email có vào Spam không?


