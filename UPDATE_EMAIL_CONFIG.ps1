# 🔐 Script Cập Nhật Cấu Hình Email Gmail
# Sử dụng script này để cập nhật App Password mà không cần chỉnh sửa file trực tiếp

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CẬP NHẬT CẤU HÌNH EMAIL GMAIL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Hiển thị hướng dẫn
Write-Host "⚠️  QUAN TRỌNG:" -ForegroundColor Yellow
Write-Host "   - App Password phải là 16 ký tự (KHÔNG phải mật khẩu Gmail thông thường)" -ForegroundColor Yellow
Write-Host "   - Tạo App Password tại: https://myaccount.google.com/apppasswords" -ForegroundColor Yellow
Write-Host "   - Bạn phải BẬT xác thực 2 bước trước khi tạo App Password" -ForegroundColor Yellow
Write-Host ""

# Nhập Email
Write-Host "📧 Bước 1: Nhập địa chỉ Email Gmail" -ForegroundColor Green
$email = Read-Host "   Nhập email (ví dụ: your-email@gmail.com)"

if ([string]::IsNullOrWhiteSpace($email)) {
    Write-Host "❌ Email không được để trống!" -ForegroundColor Red
    pause
    exit
}

# Kiểm tra định dạng email
if ($email -notmatch "^[a-zA-Z0-9._%+-]+@gmail\.com$") {
    Write-Host "⚠️  Cảnh báo: Email không có định dạng @gmail.com" -ForegroundColor Yellow
    $confirm = Read-Host "   Bạn có chắc chắn muốn tiếp tục? (y/n)"
    if ($confirm -ne "y") {
        Write-Host "❌ Đã hủy!" -ForegroundColor Red
        pause
        exit
    }
}

Write-Host ""

# Nhập App Password
Write-Host "🔑 Bước 2: Nhập App Password (16 ký tự)" -ForegroundColor Green
Write-Host "   Ví dụ: abcdefghijklmnop (hoặc abcd efgh ijkl mnop - script sẽ tự xóa khoảng trắng)" -ForegroundColor Gray
$appPassword = Read-Host "   Nhập App Password" -AsSecureString
$appPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($appPassword)
)

# Xóa khoảng trắng
$appPasswordClean = $appPasswordPlain -replace '\s', ''

if ([string]::IsNullOrWhiteSpace($appPasswordClean)) {
    Write-Host "❌ App Password không được để trống!" -ForegroundColor Red
    pause
    exit
}

if ($appPasswordClean.Length -ne 16) {
    Write-Host "⚠️  Cảnh báo: App Password thường có 16 ký tự, bạn đã nhập $($appPasswordClean.Length) ký tự" -ForegroundColor Yellow
    $confirm = Read-Host "   Bạn có chắc chắn muốn tiếp tục? (y/n)"
    if ($confirm -ne "y") {
        Write-Host "❌ Đã hủy!" -ForegroundColor Red
        pause
        exit
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📋 XÁC NHẬN THÔNG TIN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Email: $email" -ForegroundColor White
Write-Host "App Password: $('*' * $appPasswordClean.Length) (đã ẩn)" -ForegroundColor White
Write-Host ""

$confirm = Read-Host "Bạn có chắc chắn muốn cập nhật? (y/n)"
if ($confirm -ne "y") {
    Write-Host "❌ Đã hủy!" -ForegroundColor Red
    pause
    exit
}

# Đọc file application.yml
$configFile = "src\main\resources\application.yml"
if (-not (Test-Path $configFile)) {
    Write-Host "❌ Không tìm thấy file: $configFile" -ForegroundColor Red
    Write-Host "   Đảm bảo bạn đang chạy script từ thư mục backend-webbanmu" -ForegroundColor Yellow
    pause
    exit
}

Write-Host ""
Write-Host "🔄 Đang cập nhật cấu hình..." -ForegroundColor Yellow

try {
    # Đọc nội dung file
    $content = Get-Content $configFile -Raw
    
    # Backup file gốc
    $backupFile = "src\main\resources\application.yml.backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    Copy-Item $configFile $backupFile
    Write-Host "✅ Đã backup file gốc: $backupFile" -ForegroundColor Green
    
    # Cập nhật email
    $content = $content -replace '(?<=username:\s*\$\{MAIL_USERNAME:)[^}]+', $email
    
    # Cập nhật password
    $content = $content -replace '(?<=password:\s*\$\{MAIL_PASSWORD:)[^}]+', $appPasswordClean
    
    # Ghi lại file
    $content | Set-Content $configFile -NoNewline
    
    Write-Host "✅ Đã cập nhật cấu hình email thành công!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "📋 BƯỚC TIẾP THEO" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "1. Khởi động lại server backend:" -ForegroundColor White
    Write-Host "   .\gradlew bootRun" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Test chức năng gửi email:" -ForegroundColor White
    Write-Host "   - Tạo phiếu giảm giá cá nhân" -ForegroundColor Gray
    Write-Host "   - Chọn khách hàng có email" -ForegroundColor Gray
    Write-Host "   - Kiểm tra log: Get-Content logs\application.log -Tail 50 -Wait" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Log thành công sẽ hiển thị:" -ForegroundColor White
    Write-Host "   ✅ Email sent successfully to: customer@example.com" -ForegroundColor Green
    Write-Host ""
    
} catch {
    Write-Host "❌ Lỗi khi cập nhật file: $_" -ForegroundColor Red
    Write-Host "   File backup đã được tạo: $backupFile" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "🎉 Hoàn thành!" -ForegroundColor Green
Write-Host ""
pause

