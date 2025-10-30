# ✅ Email Service ĐÃ SẴN SÀNG!

## 🎉 Code Đã Được Sửa Hoàn Tất

Email service đã được enable và backend đang chạy thành công!

---

## 📧 Để Gửi Email Thật

### Chỉ Cần 1 Việc: Cấu Hình Gmail

#### **Option 1: Dùng Environment Variables (Khuyến nghị)**

**Trước khi chạy server:**

```powershell
# Windows PowerShell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password-16-chars"

cd backend-webbanmu
./gradlew bootRun
```

#### **Option 2: Sửa application.yml**

File: `src/main/resources/application.yml` (dòng 55-56)

```yaml
username: tdkstore@gmail.com              # ← Thay bằng email Gmail thật
password: abcdefghijklmnop                # ← Thay bằng App Password thật
```

⚠️ **Không commit** email/password lên Git!

---

## 🔑 Tạo Gmail App Password

1. Vào: https://myaccount.google.com/apppasswords
2. Chọn app **Mail**, device **Other** (TDK Store Backend)
3. Click **Generate** → Copy 16 ký tự
4. Xóa khoảng trắng: `abcd efgh ijkl mnop` → `abcdefghijklmnop`

**Yêu cầu:** Bật **2-Step Verification** trong Google Account

---

## ✅ Test Ngay

### 1. Tạo Phiếu Giảm Giá Cá Nhân

- Vào **Quản Lý Giảm Giá** → **Phiếu Giảm Giá**
- Click **Thêm mới**
- Chọn **Cá nhân** (không phải Công khai)
- Chọn **khách hàng có email**
- Click **Thêm mới**

### 2. Kiểm Tra Log

```powershell
Get-Content logs\application.log -Tail 20
```

**Nếu thành công:**
```
✅ Email sent successfully to: customer@example.com (Phiếu: PGG_xxx)
```

**Nếu chưa cấu hình:**
```
Email service is disabled. Skipping email notification.
```

**Nếu lỗi:**
```
❌ Lỗi khi gửi email thông báo phiếu giảm giá tới customer@example.com: AuthenticationFailedException
```

### 3. Kiểm Tra Inbox

- Vào email của khách hàng
- Kiểm tra **Inbox** hoặc **Spam/Junk**
- Subject: "🎉 Bạn đã nhận được phiếu giảm giá mới!"

---

## 🐛 Lỗi Thường Gặp

### ❌ AuthenticationFailedException

**Nguyên nhân:** App Password sai

**Giải pháp:**
- Kiểm tra App Password (16 ký tự, không khoảng trắng)
- Tạo lại App Password mới
- Đảm bảo đã bật 2-Factor Auth

### 📧 Email Không Nhận Được

**Nguyên nhân:** Email vào Spam

**Giải pháp:**
- Kiểm tra thư mục **Spam/Junk**
- Mark as "Not spam"

---

## 📊 Trạng Thái Hiện Tại

| Mục | Status |
|-----|--------|
| ✅ EmailService.java | Code đã uncomment |
| ✅ spring-boot-starter-mail | Dependency đã có |
| ✅ Backend Server | Đang chạy (port 8080) |
| ⚠️ Gmail Config | Cần cấu hình |

---

## 💡 Lưu Ý

- Email gửi **bất đồng bộ** (async) - không block API
- Nếu email fail, phiếu giảm giá **vẫn được tạo**
- Log sẽ ghi lại mọi lỗi để debug
- Không ảnh hưởng đến logic/cấu trúc bảng khác

---

## 🎯 Tóm Tắt

✅ **Backend đã chạy thành công**  
✅ **Email service đã enabled**  
⚠️ **Chỉ cần cấu hình Gmail credentials để gửi email thật**  
✅ **Không ảnh hưởng bảng khác**

---

## 📞 Test Nhanh (Mock Email)

Nếu chưa muốn cấu hình Gmail ngay:

```yaml
# application.yml
app:
  mail:
    enabled: false  # ← Set false để tắt email
```

Email sẽ không gửi, nhưng log vẫn ghi:
```
Email service is disabled. Skipping email notification.
```

**Khi nào cần gửi email thật:** Set `enabled: true` và cấu hình Gmail


