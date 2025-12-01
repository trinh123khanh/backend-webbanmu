# Test API cho chi tiết hóa đơn với mã hóa đơn: HD1764490693479_q2avv75w2

$baseUrl = "http://localhost:8080/api/hoa-don"
$maHoaDon = "HD1764490693479_q2avv75w2"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "TEST API CHI TIẾT HÓA ĐƠN" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Mã hóa đơn: $maHoaDon" -ForegroundColor Yellow
Write-Host ""

# Bước 1: Tìm hóa đơn theo mã để lấy ID
Write-Host "Bước 1: Tìm hóa đơn theo mã hóa đơn..." -ForegroundColor Green
$searchUrl = "$baseUrl/page?maHoaDon=$maHoaDon&size=1"
Write-Host "GET $searchUrl" -ForegroundColor Gray

try {
    $searchResponse = Invoke-RestMethod -Uri $searchUrl -Method Get -ContentType "application/json"
    
    if ($searchResponse.content -and $searchResponse.content.Count -gt 0) {
        $invoice = $searchResponse.content[0]
        $invoiceId = $invoice.id
        Write-Host "✅ Tìm thấy hóa đơn!" -ForegroundColor Green
        Write-Host "   ID: $invoiceId" -ForegroundColor White
        Write-Host "   Mã hóa đơn: $($invoice.maHoaDon)" -ForegroundColor White
        Write-Host "   Trạng thái: $($invoice.trangThai)" -ForegroundColor White
        Write-Host ""
        
        # Bước 2: Lấy chi tiết hóa đơn theo ID
        Write-Host "Bước 2: Lấy chi tiết hóa đơn theo ID..." -ForegroundColor Green
        $detailUrl = "$baseUrl/$invoiceId"
        Write-Host "GET $detailUrl" -ForegroundColor Gray
        
        $detailResponse = Invoke-RestMethod -Uri $detailUrl -Method Get -ContentType "application/json"
        
        Write-Host "✅ Chi tiết hóa đơn:" -ForegroundColor Green
        Write-Host ""
        
        # Hiển thị thông tin cơ bản
        Write-Host "=== THÔNG TIN CƠ BẢN ===" -ForegroundColor Cyan
        Write-Host "ID: $($detailResponse.id)" -ForegroundColor White
        Write-Host "Mã hóa đơn: $($detailResponse.maHoaDon)" -ForegroundColor White
        Write-Host "Ngày tạo: $($detailResponse.ngayTao)" -ForegroundColor White
        Write-Host "Trạng thái: $($detailResponse.trangThai)" -ForegroundColor White
        Write-Host ""
        
        # Hiển thị thông tin tài chính
        Write-Host "=== THÔNG TIN TÀI CHÍNH ===" -ForegroundColor Cyan
        Write-Host "Tổng tiền hàng (tongTien): $($detailResponse.tongTien) VNĐ" -ForegroundColor White
        Write-Host "Tiền giảm giá (tienGiamGia): $($detailResponse.tienGiamGia) VNĐ" -ForegroundColor White
        Write-Host "Phí giao hàng (phiGiaoHang): $($detailResponse.phiGiaoHang) VNĐ" -ForegroundColor Yellow
        Write-Host "Thành tiền (thanhTien): $($detailResponse.thanhTien) VNĐ" -ForegroundColor Yellow
        
        # Kiểm tra logic tính toán
        $tongTien = [decimal]$detailResponse.tongTien
        $tienGiamGia = [decimal]$detailResponse.tienGiamGia
        $phiGiaoHang = [decimal]$detailResponse.phiGiaoHang
        $thanhTien = [decimal]$detailResponse.thanhTien
        $calculatedThanhTien = $tongTien - $tienGiamGia + $phiGiaoHang
        
        Write-Host ""
        Write-Host "=== KIỂM TRA LOGIC TÍNH TOÁN ===" -ForegroundColor Cyan
        Write-Host "Công thức: thanhTien = tongTien - tienGiamGia + phiGiaoHang" -ForegroundColor Gray
        Write-Host "Tính toán: $tongTien - $tienGiamGia + $phiGiaoHang = $calculatedThanhTien" -ForegroundColor Gray
        if ([Math]::Abs($thanhTien - $calculatedThanhTien) -lt 0.01) {
            Write-Host "✅ Logic tính toán ĐÚNG!" -ForegroundColor Green
        } else {
            Write-Host "❌ Logic tính toán SAI! (Chênh lệch: $([Math]::Abs($thanhTien - $calculatedThanhTien)))" -ForegroundColor Red
        }
        Write-Host ""
        
        # Hiển thị thông tin khách hàng
        Write-Host "=== THÔNG TIN KHÁCH HÀNG ===" -ForegroundColor Cyan
        Write-Host "Tên khách hàng: $($detailResponse.tenKhachHang)" -ForegroundColor White
        Write-Host "Email: $($detailResponse.emailKhachHang)" -ForegroundColor White
        Write-Host "Số điện thoại: $($detailResponse.soDienThoaiKhachHang)" -ForegroundColor White
        Write-Host "Địa chỉ: $($detailResponse.diaChiChiTiet), $($detailResponse.phuongXa), $($detailResponse.quanHuyen), $($detailResponse.tinhThanh)" -ForegroundColor White
        Write-Host ""
        
        # Hiển thị danh sách sản phẩm
        Write-Host "=== DANH SÁCH SẢN PHẨM ===" -ForegroundColor Cyan
        if ($detailResponse.danhSachChiTiet -and $detailResponse.danhSachChiTiet.Count -gt 0) {
            Write-Host "Số lượng sản phẩm: $($detailResponse.danhSachChiTiet.Count)" -ForegroundColor White
            foreach ($item in $detailResponse.danhSachChiTiet) {
                Write-Host "  - $($item.tenSanPham) (SL: $($item.soLuong), Đơn giá: $($item.donGia) VNĐ)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠️ Không có sản phẩm trong danh sách" -ForegroundColor Yellow
        }
        Write-Host ""
        
        # Hiển thị toàn bộ JSON response (formatted)
        Write-Host "=== TOÀN BỘ JSON RESPONSE ===" -ForegroundColor Cyan
        $detailResponse | ConvertTo-Json -Depth 10 | Write-Host
        
    }
    else {
        Write-Host "❌ Không tìm thấy hóa đơn với mã: $maHoaDon" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ Lỗi khi gọi API: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Chi tiết: $($_.Exception)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "KẾT THÚC TEST" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan




