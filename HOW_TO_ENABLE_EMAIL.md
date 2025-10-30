# 📧 Cách Bật Email Gửi Thật

## ✅ Code Đã Được Sửa!

Email service đã được enable và sẵn sàng gửi email thật.

---

## 🔧 Còn 1 Bước Cuối: Cấu Hình Gmail

### Cách 1: Dùng Environment Variables (Khuyến nghị)

**Windows PowerShell:**
```powershell
# Thay bằng email và App Password thật của bạn
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password-16-chars"

# Sau đó chạy server
cd backend-webbanmu
./gradlew bootRun
```

**Linux/Mac:**
```bash
# Thay bằng email và App Password thật của bạn
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password-16-chars"

# Sau đó chạy server
cd backend-webbanmu
./gradlew bootRun
```

### Cách 2: Sửa Trực Tiếp application.yml

Mở `src/main/resources/application.yml`, tìm dòng 55-56:

```yaml
username: ${MAIL_USERNAME:your-email@gmail.com}
password: ${MAIL_PASSWORD:your-16-char-app-password}
```

Thay bằng:
```yaml
username: tdkstore2024@gmail.com              # Email Gmail thật
password: abcd efgh ijkl mnop                 # App Password thật
```

⚠️ **KHÔNG commit** email/password lên Git!

---

## 🔑 Tạo Gmail App Password

1. Vào https://myaccount.google.com/apppasswords
2. Chọn app: **Mail**, device: **Other** (nhập: TDK Store)
3. Click **Generate**
4. Copy **16 ký tự** (ví dụ: `abcd efgh ijkl mnop`)
5. Xóa khoảng trắng khi dùng: `abcdefghijklmnop`

**Lưu ý:** Cần bật **2-Step Verification** trước.

---

## ✅ Test Email

1. Chạy server: `./gradlew bootRun`
2. Tạo **Phiếu Giảm Giá Cá Nhân**
3. Chọn khách hàng có email
4. Click **Thêm mới**

### Kiểm Tra Log

```bash
tail -f logs/application.log
```

**Nếu thành công:**
```
✅ Email sent successfully to: customer@example.com (Phiếu: PGG_xxx)
```

**Nếu lỗi:**
```
❌ Lỗi khi gửi email thông báo phiếu giảm giá tới customer@example.com: [Chi tiết]
```

---

## 🐛 Lỗi Thường Gặp

### Lỗi: AuthenticationFailedException

**Nguyên nhân:** App Password sai

**Giải pháp:**
- Kiểm tra App Password đã copy đúng chưa
- Xóa khoảng trắng trong App Password
- Tạo lại App Password mới

### Email vào Spam

**Giải pháp:**
- Kiểm tra thư mục Spam/Junk
- Mark email as "Not spam"

---

## 💡 Tips

- Dùng environment variables cho bảo mật
- Test với email của chính bạn trước
- Kiểm tra logs để debug
- Email sẽ gửi bất đồng bộ (async), không block API

---

## 📝 Tóm Tắt

✅ Code đã sửa - Chỉ cần cấu hình Gmail  
✅ Không ảnh hưởng bảng khác  
✅ Sẵn sàng gửi email thật


