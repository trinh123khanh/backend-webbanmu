# Script to restart Spring Boot backend
Write-Host "🔄 Stopping any running backend processes..." -ForegroundColor Yellow

# Kill any Java processes running Spring Boot
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*BackendApplication*" } | Stop-Process -Force -ErrorAction SilentlyContinue

Start-Sleep -Seconds 2

Write-Host "✅ Starting backend on port 8088..." -ForegroundColor Green
Write-Host "📡 Backend will be available at: http://localhost:8088" -ForegroundColor Cyan
Write-Host "🧪 Test endpoint: http://localhost:8088/api/statistics/test" -ForegroundColor Cyan

# Start backend
./gradlew.bat bootRun


