# Script setup email cho backend
# Chạy script này mỗi khi mở terminal mới để start server

Write-Host "=== Cấu hình Email cho TDK Store Backend ===" -ForegroundColor Green
Write-Host ""

# Nhập email của bạn
$email = Read-Host "Nhập email Gmail của bạn"
$password = Read-Host "Nhập App Password Gmail của bạn (16 ký tự)" -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

# Set environment variables
$env:MAIL_USERNAME = $email
$env:MAIL_PASSWORD = $plainPassword

Write-Host ""
Write-Host "✅ Đã cấu hình email thành công!" -ForegroundColor Green
Write-Host "📧 Email: $email" -ForegroundColor Cyan
Write-Host ""
Write-Host "Để start server, chạy lệnh:" -ForegroundColor Yellow
Write-Host "  ./gradlew bootRun" -ForegroundColor White
Write-Host ""

