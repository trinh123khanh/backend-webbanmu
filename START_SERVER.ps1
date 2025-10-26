# Script để start server với email configuration
# Chỉ cần chạy: .\START_SERVER.ps1

Write-Host "🚀 Starting TDK Store Backend Server..." -ForegroundColor Green
Write-Host ""

# Set email credentials (Thay đổi theo email của bạn)
$env:MAIL_USERNAME = "tranthailinh16672004@gmail.com"  # ⬅️ THAY ĐỔI ĐÂY
$env:MAIL_PASSWORD = "Thang1667@"  # ⬅️ THAY ĐỔI ĐÂY

Write-Host "📧 Email configured: $env:MAIL_USERNAME" -ForegroundColor Cyan
Write-Host ""
Write-Host "Starting server..." -ForegroundColor Yellow
Write-Host ""

# Start server
.\gradlew bootRun

