# ✅ CHECKLIST KIỂM TRA EMAIL PHIẾU GIẢM GIÁ

## 📋 DANH SÁCH KIỂM TRA NHANH

### ✅ BACKEND - ĐÃ HOÀN THÀNH
- [x] EmailService.java có phương thức `sendPhieuGiamGiaNotification()`
- [x] EmailService sử dụng `@Async` để gửi email không đồng bộ
- [x] PhieuGiamGiaService.java có logic gửi email trong `createPhieuGiamGia()`
- [x] AsyncConfig.java đã cấu hình `@EnableAsync`
- [x] application.yml có cấu hình email (SMTP Gmail)
- [x] Try-catch riêng cho email để không ảnh hưởng transaction
- [x] Log đầy đủ quá trình gửi email
- [x] Kiểm tra email có tồn tại trước khi gửi

### ✅ FRONTEND - ĐÃ HOÀN THÀNH
- [x] Form có tùy chọn "Công khai" / "Cá nhân"
- [x] Form load danh sách khách hàng từ API
- [x] Có thể chọn nhiều khách hàng
- [x] Có bộ lọc khách hàng (giới tính, độ tuổi, điểm tích lũy...)
- [x] Gửi `selectedCustomerIds` trong request
- [x] Validation bắt buộc chọn khách hàng khi chế độ "Cá nhân"

### ✅ DATABASE - KHÔNG ẢNH HƯỞNG
- [x] Chỉ đọc dữ liệu từ bảng `khach_hang`
- [x] Không cập nhật/xóa bất kỳ bảng nào
- [x] Transaction được quản lý đúng
- [x] Email lỗi không làm rollback phiếu giảm giá

### ⚠️ CẦN CẤU HÌNH (TRƯỚC KHI TEST)
- [ ] Tạo App Password tại: https://myaccount.google.com/apppasswords
- [ ] Set biến môi trường `MAIL_USERNAME` (email Gmail của bạn)
- [ ] Set biến môi trường `MAIL_PASSWORD` (App Password 16 ký tự)
- [ ] Hoặc cập nhật trực tiếp trong `application.yml`

### 🧪 KIỂM TRA CHỨC NĂNG
- [ ] Khởi động backend: `.\START_SERVER.ps1`
- [ ] Khởi động frontend: `npm start`
- [ ] Truy cập form tạo phiếu giảm giá
- [ ] Chọn chế độ "Cá nhân"
- [ ] Chọn ít nhất 1 khách hàng
- [ ] Lưu phiếu giảm giá
- [ ] Kiểm tra log: `tail -f logs/application.log`
- [ ] Xác nhận email được gửi đến khách hàng

### 📊 KIỂM TRA KẾT QUẢ
- [ ] Phiếu giảm giá được tạo thành công
- [ ] Log hiển thị: "✅ Email sent successfully to: ..."
- [ ] Khách hàng nhận được email thông báo
- [ ] Email có đúng thông tin (mã phiếu, tên phiếu)
- [ ] Không có lỗi trong log

---

## 🚀 HƯỚNG DẪN TEST NHANH

### 1. Cấu hình Email (Chỉ làm 1 lần)
```bash
# Cách 1: Set biến môi trường (Khuyến nghị)
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-16-char-app-password

# Cách 2: Sửa trực tiếp trong application.yml
# Mở file: backend-webbanmu/src/main/resources/application.yml
# Tìm dòng 55-56 và thay thế:
#   username: your-email@gmail.com
#   password: your-16-char-app-password
```

### 2. Khởi động Backend
```bash
cd backend-webbanmu
.\START_SERVER.ps1
```

### 3. Khởi động Frontend
```bash
cd duanbanmu
npm start
```

### 4. Test Chức Năng
1. Truy cập: http://localhost:4200/phieu-giam-gia/create
2. Điền thông tin phiếu giảm giá:
   - Mã phiếu: (click nút "Tạo mã tự động")
   - Tên phiếu: "Phiếu giảm giá test email"
   - Loại: Tiền mặt
   - Giá trị giảm: 50000
   - Số lượng: (tự động = số khách hàng chọn)
   - **Trạng thái: Cá nhân** ← QUAN TRỌNG
3. Chọn ít nhất 1 khách hàng có email
4. Click "Lưu phiếu giảm giá"
5. Kiểm tra:
   - Thông báo thành công
   - Log: `tail -f backend-webbanmu/logs/application.log`
   - Email của khách hàng

### 5. Xác Nhận Kết Quả
Trong log, tìm các dòng sau:
```
✅ Email sent successfully to: customer@email.com (Phiếu: PGG_xxx)
```

Nếu thấy dòng này → Email đã được gửi thành công! ✅

---

## 🔧 XỬ LÝ LỖI

### Lỗi: "Failed to authenticate"
**Nguyên nhân:** Sai username/password hoặc chưa bật App Password

**Giải pháp:**
1. Truy cập: https://myaccount.google.com/apppasswords
2. Đăng nhập Gmail
3. Click "Create" → Chọn "Mail" → Chọn "Other"
4. Nhập tên: "Spring Boot Email"
5. Copy App Password (16 ký tự)
6. Paste vào `MAIL_PASSWORD`

### Lỗi: "Email service is disabled"
**Nguyên nhân:** Cờ `app.mail.enabled = false`

**Giải pháp:**
```yaml
# Trong application.yml
app:
  mail:
    enabled: true  # ← Đổi thành true
```

### Email vào Spam
**Giải pháp:**
- Khách hàng đánh dấu "Not Spam"
- Thêm địa chỉ gửi vào danh bạ

---

## 📝 GHI CHÚ

### Chức năng hoạt động như thế nào?
1. Admin tạo phiếu giảm giá ở chế độ "Cá nhân"
2. Admin chọn các khách hàng muốn gửi phiếu
3. Hệ thống tạo phiếu giảm giá
4. Hệ thống tự động gửi email thông báo cho từng khách hàng
5. Email chứa thông tin: Mã phiếu, Tên phiếu, Lời cảm ơn

### Email mẫu:
```
Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!

Xin chào [Tên Khách Hàng],

Chúc mừng! Bạn đã nhận được một phiếu giảm giá đặc biệt từ TDK Store.

📌 Thông tin phiếu giảm giá:
- Mã phiếu: PGG_xxx
- Tên phiếu: Phiếu giảm giá test email

Hãy sử dụng phiếu giảm giá này trong lần mua sắm tiếp theo của bạn!

Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi.

Trân trọng,
TDK Store - Bán mũ bảo hiểm
```

### Lợi ích:
- ✅ Khách hàng được thông báo ngay khi có phiếu giảm giá
- ✅ Tăng tỷ lệ sử dụng phiếu giảm giá
- ✅ Tăng trải nghiệm khách hàng
- ✅ Tự động hóa, tiết kiệm thời gian

---

**Cập nhật:** 29/10/2025  
**Trạng thái:** ✅ Hoàn thành và sẵn sàng test

