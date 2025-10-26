# 📧 Hướng Dẫn Nhanh - Cấu Hình Email

## Có 2 cách để cấu hình email:

---

## ✅ **CÁCH 1: Cấu hình trong file (KHUYẾN NGHỊ - Dễ nhất)**

### Bước 1: Mở file `application.yml`

Tìm đến dòng **112-114**:

```yaml
spring:
  mail:
    # ⚠️ THAY ĐỔI 2 DÒNG SAU ĐÂY:
    username: your-email@gmail.com        # ← Nhập email Gmail của bạn ở đây
    password: your-16-char-app-password   # ← Nhập App Password (16 ký tự)
```

### Bước 2: Thay đổi

Thay `your-email@gmail.com` bằng email thật của bạn  
Thay `your-16-char-app-password` bằng App Password

**Ví dụ:**
```yaml
username: mycompany.tdk@gmail.com
password: abcd efgh ijkl mnop
```

### Bước 3: Lưu file và start server

```bash
./gradlew bootRun
```

**Vậy là xong! ✅**

---

## ⚙️ **CÁCH 2: Dùng Environment Variables (Advanced)**

### Bước 1: Mở PowerShell trong thư mục backend-webbanmu

### Bước 2: Chạy lệnh (mỗi khi mở terminal mới):

```powershell
# Set email credentials
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-16-char-app-password"
```

### Bước 3: Start server

```powershell
./gradlew bootRun
```

**Lưu ý:** Cách này chỉ có hiệu lực trong session PowerShell hiện tại. Khi đóng terminal, phải set lại.

---

## 🔐 Cách tạo App Password cho Gmail

1. Vào [Google Account Settings](https://myaccount.google.com/)
2. Click **Security** (Bảo mật)
3. Bật **2-Step Verification** (nếu chưa bật)
4. Scroll xuống, click **App passwords**
5. Chọn app: **Mail**
6. Chọn device: **Other** → Nhập tên: "TDK Store Backend"
7. Click **Generate**
8. Copy mật khẩu 16 ký tự (không có dấu cách)

---

## 🚀 Quick Start Scripts

### Script tự động (Windows PowerShell)

1. Mở file `START_SERVER.ps1`
2. Thay đổi email và password ở dòng 6-7
3. Chạy: `.\START_SERVER.ps1`

### Script interactive

1. Chạy: `.\SETUP_EMAIL.ps1`
2. Nhập email và password khi được hỏi
3. Script sẽ tự động start server

---

## ❓ FAQ

### Q: Tại sao không gửi được email?
**A:** Kiểm tra:
1. Email và password đã nhập đúng chưa?
2. App Password đã tạo đúng chưa (16 ký tự)?
3. Đã bật 2-Step Verification chưa?
4. Kiểm tra log: `logs/application.log`

### Q: Có thể dùng email khác ngoài Gmail không?
**A:** Có! Thay đổi cấu hình trong `application.yml`:

```yaml
spring:
  mail:
    host: smtp.your-email-provider.com  # Ví dụ: smtp.outlook.com
    port: 587
    username: your-email@provider.com
    password: your-password
```

### Q: Làm sao tắt gửi email tạm thời?
**A:** Trong `application.yml`, tìm:

```yaml
app:
  mail:
    enabled: false  # ← Set false
```

### Q: Email bị vào Spam?
**A:** 
- Thêm email vào whitelist
- Kiểm tra folder Spam
- Đợi một chút (có thể delay)

---

## 📝 Lưu ý quan trọng

⚠️ **KHÔNG commit file `application.yml` lên Git nếu có chứa mật khẩu thật!**

Nên dùng:
- `.env` file (local)
- Environment variables
- Secrets management tools

---

## 🎯 Tóm tắt

**Cách đơn giản nhất:**

1. Mở `application.yml`
2. Thay đổi dòng 113-114
3. Chạy `./gradlew bootRun`
4. Xong! ✅

