# 🔧 Hướng Dẫn Enable Tính Năng Email & Excel

## Tình trạng hiện tại

✅ **Đã fix lỗi compile** - Server có thể chạy được  
⚠️ **Email Service**: Đang ở chế độ simulation (chỉ log)  
⚠️ **Excel Export**: Đang ở chế độ simulation (chỉ log)

---

## 📧 Enable Email Service

### Bước 1: Uncomment code trong `EmailService.java`

Mở file: `src/main/java/com/example/backend/service/EmailService.java`

**Tìm dòng 15 và uncomment:**
```java
// private final JavaMailSender mailSender;  ← Xóa comment này
private final JavaMailSender mailSender;
```

**Tìm dòng 48-54 và uncomment:**
```java
// TODO: Uncomment khi đã cấu hình email đúng
SimpleMailMessage message = new SimpleMailMessage();
message.setFrom(fromEmail);
message.setTo(customerEmail);
message.setSubject("🎉 Bạn đã nhận được phiếu giảm giá mới!");
message.setText(emailContent);
mailSender.send(message);
```

**Comment lại dòng 56-60:**
```java
// log.info("📧 EMAIL NOTIFICATION (Simulated):");
// log.info("   To: {}", customerEmail);
// log.info("   Subject: 🎉 Bạn đã nhận được phiếu giảm giá mới!");
// log.info("   Content: {}", emailContent);
// log.info("✅ Email notification logged successfully for: {}", customerEmail);
```

### Bước 2: Cấu hình email

Trong `application.yml`, dòng 113-114:
```yaml
username: your-email@gmail.com        # ← Nhập email thật
password: your-16-char-app-password   # ← Nhập App Password thật
```

### Bước 3: Build và test

```bash
./gradlew build -x test
./gradlew bootRun
```

---

## 📊 Enable Excel Export

### Bước 1: Uncomment code trong `ExcelExportService.java`

Mở file: `src/main/java/com/example/backend/service/ExcelExportService.java`

**Tìm dòng 3-5 và uncomment:**
```java
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
```

**Tìm dòng 20 và uncomment:**
```java
// TODO: Uncomment khi Apache POI đã được cấu hình đúng
/*
Workbook workbook = new XSSFWorkbook();
```

**Tìm dòng 95 và uncomment:**
```java
*/
// Convert to base64
byte[] excelBytes = outputStream.toByteArray();
return Base64.getEncoder().encodeToString(excelBytes);
```

**Comment lại dòng 97-99:**
```java
// System.out.println("📊 Excel Export Service - Simulated export for " + data.size() + " records");
// return Base64.getEncoder().encodeToString("Excel export simulated".getBytes());
```

### Bước 2: Build và test

```bash
./gradlew build -x test
./gradlew bootRun
```

---

## 🚀 Quick Enable Script

Tạo script để enable tất cả:

```bash
# Enable Email
sed -i 's|// private final JavaMailSender|private final JavaMailSender|g' src/main/java/com/example/backend/service/EmailService.java

# Enable Excel
sed -i 's|// import org.apache.poi|import org.apache.poi|g' src/main/java/com/example/backend/service/ExcelExportService.java

# Build
./gradlew build -x test
```

---

## ✅ Kiểm tra tính năng

### Test Email:
1. Tạo phiếu giảm giá cá nhân
2. Chọn khách hàng có email
3. Kiểm tra log: `logs/application.log`
4. Kiểm tra inbox khách hàng

### Test Excel:
1. Vào trang quản lý phiếu giảm giá
2. Click nút Export Excel
3. Kiểm tra file download

---

## 🔍 Troubleshooting

### Email không gửi được:
- Kiểm tra App Password Gmail
- Kiểm tra 2-Factor Authentication
- Xem log lỗi trong `application.log`

### Excel không export được:
- Kiểm tra Apache POI dependency
- Xem log lỗi trong console

### Build lỗi:
```bash
./gradlew clean build -x test
```

---

## 📝 Lưu ý

- **Development**: Có thể để simulation mode để test logic
- **Production**: Phải enable đầy đủ tính năng
- **Security**: Không commit email/password lên Git

---

## 🎯 Tóm tắt

**Hiện tại**: Server chạy được, tính năng ở chế độ simulation  
**Khi cần**: Uncomment code và cấu hình credentials  
**Kết quả**: Email thật + Excel export thật
