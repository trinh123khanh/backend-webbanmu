# ⚠️ LỖI GỬI EMAIL - CẦN SỬA NGAY

## 🔴 VẤN ĐỀ HIỆN TẠI

Email **KHÔNG GỬI ĐƯỢC** vì lỗi xác thực:

```
❌ Authentication failed: Username and Password not accepted
```

**Nguyên nhân**: Password `Thang1667@` trong `application.yml` **KHÔNG PHẢI** là App Password hợp lệ của Gmail!

---

## ✅ GIẢI PHÁP - 3 BƯỚC ĐỂ SỬA

### 🔐 BƯỚC 1: TẠO APP PASSWORD GMAIL

App Password là mật khẩu **16 ký tự** do Google tạo ra (KHÔNG phải mật khẩu Gmail thông thường)

1. **Bật xác thực 2 bước** (nếu chưa có):
   - Truy cập: https://myaccount.google.com/security
   - Tìm "2-Step Verification" → Bật

2. **Tạo App Password**:
   - Truy cập: https://myaccount.google.com/apppasswords
   - Tạo password mới với tên: "TDK Store Backend"
   - Google sẽ hiển thị 16 ký tự, ví dụ: `abcd efgh ijkl mnop`
   - **SAO CHÉP** ngay (bạn chỉ thấy 1 lần!)

📖 **Chi tiết**: Đọc file `HUONG_DAN_TAO_APP_PASSWORD_GMAIL.md`

---

### ⚙️ BƯỚC 2: CẬP NHẬT CẤU HÌNH

#### Cách 1: Dùng Script Tự Động (KHUYẾN NGHỊ)

```powershell
cd backend-webbanmu
.\UPDATE_EMAIL_CONFIG.ps1
```

Script sẽ hỏi:
- Email Gmail của bạn
- App Password vừa tạo (16 ký tự)

#### Cách 2: Chỉnh Sửa Thủ Công

Mở file: `backend-webbanmu/src/main/resources/application.yml`

**Dòng 55-56**, sửa thành:

```yaml
# TRƯỚC (❌ SAI)
username: ${MAIL_USERNAME:tranthailinh16672004@gmail.com}
password: ${MAIL_PASSWORD:Thang1667@}   # ← Mật khẩu Gmail thông thường (SAI!)

# SAU (✅ ĐÚNG)
username: ${MAIL_USERNAME:tranthailinh16672004@gmail.com}
password: ${MAIL_PASSWORD:abcdefghijklmnop}   # ← App Password 16 ký tự (ĐÚNG!)
```

⚠️ **Lưu ý**: Thay `abcdefghijklmnop` bằng App Password thật của bạn (KHÔNG có khoảng trắng!)

---

### 🚀 BƯỚC 3: KHỞI ĐỘNG LẠI SERVER & TEST

#### 3.1. Khởi động lại server

```powershell
cd backend-webbanmu
.\gradlew bootRun
```

Hoặc sử dụng script:

```powershell
.\START_SERVER.ps1
```

#### 3.2. Test chức năng gửi email

1. Mở trình duyệt → Vào "Quản lý Phiếu Giảm Giá"
2. Click "Thêm Phiếu Giảm Giá"
3. Chọn loại **"Cá nhân"**
4. Chọn 1-2 khách hàng (đảm bảo có email)
5. Điền thông tin và click "Thêm mới"

#### 3.3. Kiểm tra log

```powershell
Get-Content backend-webbanmu\logs\application.log -Tail 50 -Wait
```

**Thành công** nếu thấy:
```
✅ Email sent successfully to: customer@example.com (Phiếu: PGG_XXX)
```

**Thất bại** nếu thấy:
```
❌ Lỗi khi gửi email: Authentication failed
```

---

## 📊 TÓM TẮT KIỂM TRA

### ✅ Checklist Hoàn Thành

- [ ] Đã bật xác thực 2 bước cho Gmail
- [ ] Đã tạo App Password (16 ký tự)
- [ ] Đã cập nhật `application.yml` với App Password
- [ ] Đã khởi động lại server
- [ ] Test tạo phiếu giảm giá cá nhân → Email gửi thành công

---

## 🔍 THÔNG TIN KỸ THUẬT

### Cấu hình hiện tại

**File**: `backend-webbanmu/src/main/resources/application.yml`

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:tranthailinh16672004@gmail.com}
    password: ${MAIL_PASSWORD:Thang1667@}  # ← ĐÂY LÀ VẤN ĐỀ!
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  mail:
    enabled: true  # Email đang BẬT
```

### Lỗi trong log

```
jakarta.mail.AuthenticationFailedException: 
535-5.7.8 Username and Password not accepted. 
For more information, go to
535 5.7.8 https://support.google.com/mail/?p=BadCredentials
```

### Các file liên quan

- **EmailService.java**: Logic gửi email (✅ Đã đúng)
- **PhieuGiamGiaService.java**: Gửi email khi tạo phiếu cá nhân (✅ Đã đúng)
- **AsyncConfig.java**: Cấu hình async executor (✅ Đã đúng)
- **application.yml**: Cấu hình email (❌ Password SAI!)

---

## 🆘 TẮT EMAIL TẠM THỜI (NẾU CẦN)

Nếu bạn muốn tắt tính năng email tạm thời để test các chức năng khác:

**File**: `application.yml` → Dòng 128:

```yaml
app:
  mail:
    enabled: false  # Tắt gửi email
```

Khởi động lại server. Phiếu giảm giá vẫn tạo được nhưng không gửi email.

---

## 📞 HỖ TRỢ THÊM

### Tài liệu chi tiết:
- `HUONG_DAN_TAO_APP_PASSWORD_GMAIL.md` - Hướng dẫn tạo App Password từng bước
- `BAO_CAO_KIEM_TRA_EMAIL_PHIEU_GIAM_GIA.md` - Báo cáo đầy đủ về tính năng email

### Script hỗ trợ:
- `UPDATE_EMAIL_CONFIG.ps1` - Cập nhật cấu hình email tự động
- `START_SERVER.ps1` - Khởi động server

### Liên kết hữu ích:
- Tạo App Password: https://myaccount.google.com/apppasswords
- Bật xác thực 2 bước: https://myaccount.google.com/security
- Hướng dẫn của Google: https://support.google.com/mail/?p=BadCredentials

---

## 🎯 HÀNH ĐỘNG NGAY

```powershell
# 1. Tạo App Password tại: https://myaccount.google.com/apppasswords

# 2. Cập nhật cấu hình
cd backend-webbanmu
.\UPDATE_EMAIL_CONFIG.ps1

# 3. Khởi động lại server
.\gradlew bootRun

# 4. Kiểm tra log (Terminal mới)
Get-Content logs\application.log -Tail 50 -Wait

# 5. Test tạo phiếu giảm giá cá nhân trên giao diện web
```

---

## ✨ KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành, log sẽ hiển thị:

```
2025-10-30 10:30:15 [main] INFO c.e.b.BackendApplication - Started BackendApplication in 8.123 seconds
2025-10-30 10:31:20 [http-nio-8080-exec-1] INFO c.e.b.s.PhieuGiamGiaService - Bắt đầu gửi email thông báo cho 3 khách hàng
2025-10-30 10:31:21 [email-1] INFO c.e.b.s.EmailService - ✅ Email sent successfully to: customer1@gmail.com (Phiếu: PGG_001)
2025-10-30 10:31:22 [email-2] INFO c.e.b.s.EmailService - ✅ Email sent successfully to: customer2@gmail.com (Phiếu: PGG_001)
2025-10-30 10:31:23 [email-1] INFO c.e.b.s.EmailService - ✅ Email sent successfully to: customer3@gmail.com (Phiếu: PGG_001)
2025-10-30 10:31:23 [email-1] INFO c.e.b.s.EmailService - Hoàn thành gửi email thông báo
```

🎉 **THÀNH CÔNG!** Email đã hoạt động!

