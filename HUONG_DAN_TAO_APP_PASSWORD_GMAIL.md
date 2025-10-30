# 🔐 HƯỚNG DẪN TẠO APP PASSWORD GMAIL

## ⚠️ QUAN TRỌNG
App Password là mật khẩu 16 ký tự đặc biệt do Google tạo ra, **KHÔNG PHẢI** là mật khẩu Gmail thông thường của bạn!

---

## 📋 BƯỚC 1: BẬT XÁC THỰC 2 BƯỚC (2-STEP VERIFICATION)

App Password chỉ hoạt động khi bạn đã bật xác thực 2 bước cho tài khoản Gmail.

1. Truy cập: https://myaccount.google.com/security
2. Tìm phần **"How you sign in to Google"** (Cách bạn đăng nhập vào Google)
3. Click vào **"2-Step Verification"** (Xác minh 2 bước)
4. Làm theo hướng dẫn để bật tính năng này
5. **Lưu ý**: Bạn cần số điện thoại để nhận mã xác thực

---

## 📋 BƯỚC 2: TẠO APP PASSWORD

### Cách 1: Truy cập trực tiếp
1. Truy cập: https://myaccount.google.com/apppasswords
2. Đăng nhập vào tài khoản Gmail của bạn (nếu được yêu cầu)

### Cách 2: Từ trang Security
1. Truy cập: https://myaccount.google.com/security
2. Tìm phần **"How you sign in to Google"**
3. Click vào **"App passwords"** (Mật khẩu ứng dụng)

### Tạo App Password:
1. Trong màn hình "App passwords":
   - **App name**: Nhập tên (ví dụ: "TDK Store Backend Email")
   - Click **"Create"** (Tạo)

2. Google sẽ hiển thị một mật khẩu 16 ký tự, dạng:
   ```
   xxxx xxxx xxxx xxxx
   ```
   Ví dụ: `abcd efgh ijkl mnop`

3. **SAO CHÉP** mật khẩu này ngay (không có dấu cách)
   - Ví dụ: `abcdefghijklmnop`

4. Click **"Done"**

⚠️ **CHÚ Ý**: Bạn chỉ thấy mật khẩu này 1 lần duy nhất! Hãy sao chép ngay.

---

## 📋 BƯỚC 3: CẬP NHẬT APPLICATION.YML

### File: `backend-webbanmu/src/main/resources/application.yml`

Cập nhật 2 dòng sau:

```yaml
# Email Configuration
mail:
  host: smtp.gmail.com
  port: 587
  username: ${MAIL_USERNAME:EMAIL_CUA_BAN@gmail.com}        # ← Thay bằng email Gmail của bạn
  password: ${MAIL_PASSWORD:APP_PASSWORD_16_KY_TU}          # ← Thay bằng App Password vừa tạo (KHÔNG có dấu cách)
```

### Ví dụ cụ thể:

**TRƯỚC** (❌ SAI):
```yaml
username: ${MAIL_USERNAME:tranthailinh16672004@gmail.com}
password: ${MAIL_PASSWORD:Thang1667@}   # ← Mật khẩu Gmail thông thường (SAI!)
```

**SAU** (✅ ĐÚNG):
```yaml
username: ${MAIL_USERNAME:tranthailinh16672004@gmail.com}
password: ${MAIL_PASSWORD:abcdefghijklmnop}   # ← App Password 16 ký tự (ĐÚNG!)
```

---

## 📋 BƯỚC 4: KHỞI ĐỘNG LẠI SERVER

Sau khi cập nhật `application.yml`:

```powershell
# Dừng server hiện tại (Ctrl+C nếu đang chạy)

# Khởi động lại server
cd backend-webbanmu
.\gradlew bootRun
```

---

## 📋 BƯỚC 5: TEST CHỨC NĂNG GỬI EMAIL

1. Mở trình duyệt, truy cập ứng dụng frontend
2. Vào trang **"Quản lý Phiếu Giảm Giá"**
3. Click **"Thêm Phiếu Giảm Giá"**
4. Chọn loại **"Cá nhân"**
5. Chọn một vài khách hàng (đảm bảo khách hàng có email)
6. Điền thông tin phiếu giảm giá và click **"Thêm mới"**

### Kiểm tra log:

```powershell
# Xem log realtime
Get-Content backend-webbanmu\logs\application.log -Tail 50 -Wait
```

### Log thành công sẽ hiển thị:
```
✅ Email sent successfully to: customer@example.com (Phiếu: PGG_XXX)
Đã gửi email thông báo tới khách hàng Nguyễn Văn A (customer@example.com)
Hoàn thành gửi email thông báo
```

### Log lỗi sẽ hiển thị:
```
❌ Lỗi khi gửi email thông báo phiếu giảm giá tới customer@example.com: Authentication failed
```

---

## 🔍 KHẮC PHỤC SỰ CỐ

### Lỗi: "Username and Password not accepted"

**Nguyên nhân**:
- App Password không đúng
- Chưa bật xác thực 2 bước
- Sao chép App Password có khoảng trắng

**Giải pháp**:
1. Xóa App Password cũ tại: https://myaccount.google.com/apppasswords
2. Tạo App Password mới
3. Sao chép **KHÔNG có khoảng trắng**: `abcdefghijklmnop`
4. Cập nhật lại `application.yml`

---

### Lỗi: "Less secure app access"

**Giải pháp**:
- Google đã ngừng hỗ trợ "Less secure app" từ 2022
- **BẮT BUỘC** phải dùng App Password
- Không thể dùng mật khẩu Gmail thông thường

---

### Lỗi: "App passwords" không hiển thị

**Nguyên nhân**:
- Chưa bật xác thực 2 bước
- Tài khoản Workspace/Organization có chính sách hạn chế

**Giải pháp**:
1. Bật xác thực 2 bước trước: https://myaccount.google.com/security
2. Nếu là tài khoản tổ chức, liên hệ admin IT

---

## ✅ CHECKLIST HOÀN TẤT

- [ ] Đã bật xác thực 2 bước cho Gmail
- [ ] Đã tạo App Password thành công
- [ ] Đã sao chép App Password (16 ký tự, không có khoảng trắng)
- [ ] Đã cập nhật `application.yml` với:
  - Email Gmail đúng
  - App Password đúng
- [ ] Đã khởi động lại server
- [ ] Đã test tạo phiếu giảm giá cá nhân
- [ ] Log hiển thị "✅ Email sent successfully"

---

## 📞 HỖ TRỢ

Nếu vẫn gặp vấn đề:

1. **Kiểm tra log chi tiết**:
   ```powershell
   Get-Content backend-webbanmu\logs\application.log -Tail 100
   ```

2. **Kiểm tra cấu hình**:
   - File: `backend-webbanmu/src/main/resources/application.yml`
   - Dòng 55-56: username và password

3. **Tạm thời tắt email** (để test các chức năng khác):
   ```yaml
   app:
     mail:
       enabled: false  # Tắt gửi email
   ```

---

## 🎉 THÀNH CÔNG!

Khi bạn thấy log này, email đã hoạt động:

```
2025-10-30 10:30:15 [email-1] INFO c.e.b.s.EmailService - Bắt đầu gửi email thông báo cho 3 khách hàng
2025-10-30 10:30:16 [email-1] INFO c.e.b.s.EmailService - ✅ Email sent successfully to: customer1@example.com (Phiếu: PGG_001)
2025-10-30 10:30:17 [email-2] INFO c.e.b.s.EmailService - ✅ Email sent successfully to: customer2@example.com (Phiếu: PGG_001)
2025-10-30 10:30:18 [email-1] INFO c.e.b.s.EmailService - ✅ Email sent successfully to: customer3@example.com (Phiếu: PGG_001)
2025-10-30 10:30:18 [email-1] INFO c.e.b.s.EmailService - Hoàn thành gửi email thông báo
```

Khách hàng sẽ nhận được email với nội dung:

```
Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!

Xin chào [Tên Khách Hàng],

Chúc mừng! Bạn đã nhận được một phiếu giảm giá đặc biệt từ TDK Store.

📌 Thông tin phiếu giảm giá:
- Mã phiếu: PGG_001
- Tên phiếu: Giảm giá mùa hè

Hãy sử dụng phiếu giảm giá này trong lần mua sắm tiếp theo của bạn!

Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi.

Trân trọng,
TDK Store - Bán mũ bảo hiểm
```

