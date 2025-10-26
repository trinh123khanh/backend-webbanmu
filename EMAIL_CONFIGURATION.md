# 📧 Hướng Dẫn Cấu Hình Email

## Tổng quan

Tính năng gửi email tự động được thêm vào hệ thống để thông báo cho khách hàng khi họ nhận được phiếu giảm giá cá nhân mới.

## Tính năng

- ✅ Gửi email thông báo tự động khi tạo phiếu giảm giá cá nhân thành công
- ✅ Lấy email từ ID khách hàng đã chọn
- ✅ Gửi email cho nhiều khách hàng cùng lúc
- ✅ Không ảnh hưởng đến logic tạo phiếu giảm giá nếu email lỗi
- ✅ Hỗ trợ async để không làm chậm response

## Cấu hình Email

### 1. Cấu hình trong `application.yml`

File đã được cập nhật với cấu hình mặc định:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  mail:
    enabled: true # Set false để tắt gửi email
```

### 2. Cấu hình Gmail (Khuyến nghị)

#### Bước 1: Bật 2-Factor Authentication
1. Vào Google Account Settings
2. Security > 2-Step Verification
3. Bật 2-Step Verification

#### Bước 2: Tạo App Password
1. Security > App passwords
2. Chọn app: "Mail"
3. Chọn device: "Other (Custom name)"
4. Nhập tên: "TDK Store Backend"
5. Click Generate
6. Copy App Password (16 ký tự)

#### Bước 3: Cấu hình Environment Variables

**Windows:**
```powershell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-16-char-app-password"
```

**Linux/Mac:**
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-16-char-app-password"
```

### 3. Tắt tính năng gửi email (Development)

Để tắt tính năng gửi email trong quá trình development, thay đổi trong `application.yml`:

```yaml
app:
  mail:
    enabled: false
```

## Luồng hoạt động

1. Admin tạo phiếu giảm giá cá nhân và chọn khách hàng
2. System tạo phiếu giảm giá vào database
3. System tạo các bản ghi cá nhân trong bảng `phieu_giam_gia_ca_nhan`
4. System lấy email của từng khách hàng đã chọn
5. System gửi email thông báo cho từng khách hàng (async)
6. Nếu gửi email thất bại, vẫn tiếp tục (không rollback transaction)

## Nội dung Email

Email sẽ có nội dung:

```
Xin chào [Tên khách hàng],

Chúc mừng! Bạn đã nhận được một phiếu giảm giá đặc biệt từ TDK Store.

📌 Thông tin phiếu giảm giá:
- Mã phiếu: [Mã phiếu]
- Tên phiếu: [Tên phiếu]

Hãy sử dụng phiếu giảm giá này trong lần mua sắm tiếp theo của bạn!

Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi.

Trân trọng,
TDK Store - Bán mũ bảo hiểm
```

## Files đã thêm/sửa đổi

1. ✅ `EmailService.java` - Service gửi email
2. ✅ `AsyncConfig.java` - Cấu hình async
3. ✅ `PhieuGiamGiaService.java` - Thêm logic gửi email khi tạo phiếu cá nhân
4. ✅ `build.gradle` - Thêm dependency spring-boot-starter-mail
5. ✅ `application.yml` - Thêm cấu hình email

## Kiểm tra tính năng

1. Build lại project:
```bash
cd backend-webbanmu
./gradlew clean build
```

2. Chạy server:
```bash
./gradlew bootRun
```

3. Test bằng cách tạo phiếu giảm giá cá nhân:
- Vào giao diện "Thêm Phiếu Giảm Giá"
- Chọn chế độ "Cá nhân"
- Chọn ít nhất 1 khách hàng
- Điền thông tin và submit

4. Kiểm tra log:
- Look for: "Đã gửi email thông báo tới khách hàng..."
- Check inbox của khách hàng đã chọn

## Troubleshooting

### Email không gửi được
1. Kiểm tra `app.mail.enabled` có bằng `true` không
2. Kiểm tra `MAIL_USERNAME` và `MAIL_PASSWORD` đã set đúng chưa
3. Kiểm tra App Password của Gmail đã tạo đúng chưa (16 ký tự)
4. Kiểm tra log để xem lỗi cụ thể

### Lỗi "Authentication failed"
- Đảm bảo đã bật 2-Factor Authentication
- Đảm bảo App Password đã được tạo đúng cách
- Thử tạo App Password mới

### Email bị vào Spam
- Thêm email sender vào whitelist
- Kiểm tra Gmail spam folder

## Lưu ý

- Email được gửi async, không block response
- Nếu email fail, phiếu giảm giá vẫn được tạo thành công
- Chỉ gửi email cho khách hàng có email hợp lệ
- Log chi tiết được ghi trong application.log

