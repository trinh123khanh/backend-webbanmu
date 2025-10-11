# API Test Commands cho DotGiamGia

## Base URL
```
http://localhost:8080/api/dot-giam-gia
```

## 1. Tạo đợt giảm giá mới (POST)

```bash
curl -X POST http://localhost:8080/api/dot-giam-gia \
  -H "Content-Type: application/json" \
  -d '{
    "maDotGiamGia": "DGG001",
    "tenDotGiamGia": "Đợt giảm giá Black Friday",
    "loaiDotGiamGia": "PHAN_TRAM",
    "giaTriDotGiam": "20",
    "soTien": null,
    "moTa": "Đợt giảm giá lớn nhất trong năm",
    "ngayBatDau": "2024-12-01T00:00:00",
    "ngayKetThuc": "2024-12-31T23:59:59",
    "soLuongSuDung": 1000,
    "trangThai": true
  }'
```

## 2. Tạo đợt giảm giá theo số tiền (POST)

```bash
curl -X POST http://localhost:8080/api/dot-giam-gia \
  -H "Content-Type: application/json" \
  -d '{
    "maDotGiamGia": "DGG002",
    "tenDotGiamGia": "Giảm giá cố định",
    "loaiDotGiamGia": "SO_TIEN",
    "giaTriDotGiam": null,
    "soTien": 50000,
    "moTa": "Giảm 50k cho đơn hàng từ 500k",
    "ngayBatDau": "2024-12-01T00:00:00",
    "ngayKetThuc": "2024-12-15T23:59:59",
    "soLuongSuDung": 500,
    "trangThai": true
  }'
```

## 3. Lấy đợt giảm giá theo ID (GET)

```bash
curl -X GET http://localhost:8080/api/dot-giam-gia/1
```


## 4. Lấy đợt giảm giá theo mã (GET)

```bash
curl -X GET http://localhost:8080/api/dot-giam-gia/ma/DGG001
```

## 5. Cập nhật đợt giảm giá (PUT)

```bash
curl -X PUT http://localhost:8080/api/dot-giam-gia/1 \
  -H "Content-Type: application/json" \
  -d '{
    "maDotGiamGia": "DGG001",
    "tenDotGiamGia": "Đợt giảm giá Black Friday - Cập nhật",
    "loaiDotGiamGia": "PHAN_TRAM",
    "giaTriDotGiam": "25",
    "soTien": null,
    "moTa": "Đợt giảm giá lớn nhất trong năm - Tăng lên 25%",
    "ngayBatDau": "2024-12-01T00:00:00",
    "ngayKetThuc": "2024-12-31T23:59:59",
    "soLuongSuDung": 1500,
    "trangThai": true
  }'
```

## 6. Lấy danh sách đợt giảm giá có phân trang (GET)

```bash
# Lấy trang đầu tiên, 10 phần tử
curl -X GET "http://localhost:8080/api/dot-giam-gia?page=0&size=10&sortBy=id&sortDir=desc"

# Lấy trang thứ 2, 5 phần tử, sắp xếp theo tên
curl -X GET "http://localhost:8080/api/dot-giam-gia?page=1&size=5&sortBy=tenDotGiamGia&sortDir=asc"
```

## 7. Lấy tất cả đợt giảm giá không phân trang (GET)

```bash
curl -X GET http://localhost:8080/api/dot-giam-gia/all
```

## 8. Tìm kiếm đợt giảm giá (GET)

```bash
# Tìm kiếm theo tên
curl -X GET "http://localhost:8080/api/dot-giam-gia/search?tenDotGiamGia=Black&page=0&size=10"

# Tìm kiếm theo mã
curl -X GET "http://localhost:8080/api/dot-giam-gia/search?maDotGiamGia=DGG&page=0&size=10"

# Tìm kiếm theo trạng thái
curl -X GET "http://localhost:8080/api/dot-giam-gia/search?trangThai=true&page=0&size=10"

# Tìm kiếm theo loại
curl -X GET "http://localhost:8080/api/dot-giam-gia/search?loaiDotGiamGia=PHAN_TRAM&page=0&size=10"

# Tìm kiếm tổng hợp
curl -X GET "http://localhost:8080/api/dot-giam-gia/search?tenDotGiamGia=Black&trangThai=true&page=0&size=10"
```

## 9. Lấy đợt giảm giá theo trạng thái (GET)

```bash
# Lấy đợt giảm giá đang hoạt động
curl -X GET http://localhost:8080/api/dot-giam-gia/trang-thai/true

# Lấy đợt giảm giá đã tắt
curl -X GET http://localhost:8080/api/dot-giam-gia/trang-thai/false
```

## 10. Lấy đợt giảm giá theo loại (GET)

```bash
# Lấy đợt giảm giá theo phần trăm
curl -X GET http://localhost:8080/api/dot-giam-gia/loai/PHAN_TRAM

# Lấy đợt giảm giá theo số tiền
curl -X GET http://localhost:8080/api/dot-giam-gia/loai/SO_TIEN
```

## 11. Lấy đợt giảm giá đang hoạt động (GET)

```bash
curl -X GET http://localhost:8080/api/dot-giam-gia/active
```

## 12. Kiểm tra mã đợt giảm giá tồn tại (GET)

```bash
# Kiểm tra mã đã tồn tại
curl -X GET http://localhost:8080/api/dot-giam-gia/exists/ma/DGG001

# Kiểm tra mã chưa tồn tại
curl -X GET http://localhost:8080/api/dot-giam-gia/exists/ma/DGG999
```

## 13. Xóa đợt giảm giá (DELETE)

```bash
curl -X DELETE http://localhost:8080/api/dot-giam-gia/1
```

## 14. Kiểm tra Swagger UI

```bash
# Mở trình duyệt và truy cập:
# http://localhost:8080/swagger-ui.html
```

## 15. Kiểm tra API Docs

```bash
curl -X GET http://localhost:8080/v3/api-docs
```

## Test Cases để thử:

### 1. Test Validation Errors:
```bash
# Tạo với mã trùng
curl -X POST http://localhost:8080/api/dot-giam-gia \
  -H "Content-Type: application/json" \
  -d '{
    "maDotGiamGia": "DGG001",
    "tenDotGiamGia": "Test trùng mã",
    "ngayBatDau": "2024-12-01T00:00:00",
    "ngayKetThuc": "2024-12-31T23:59:59",
    "soLuongSuDung": 100,
    "trangThai": true
  }'
```

### 2. Test Date Validation:
```bash
# Ngày bắt đầu sau ngày kết thúc
curl -X POST http://localhost:8080/api/dot-giam-gia \
  -H "Content-Type: application/json" \
  -d '{
    "maDotGiamGia": "DGG003",
    "tenDotGiamGia": "Test ngày sai",
    "ngayBatDau": "2024-12-31T23:59:59",
    "ngayKetThuc": "2024-12-01T00:00:00",
    "soLuongSuDung": 100,
    "trangThai": true
  }'
```

### 3. Test Required Fields:
```bash
# Thiếu trường bắt buộc
curl -X POST http://localhost:8080/api/dot-giam-gia \
  -H "Content-Type: application/json" \
  -d '{
    "maDotGiamGia": "DGG004",
    "ngayBatDau": "2024-12-01T00:00:00",
    "ngayKetThuc": "2024-12-31T23:59:59",
    "soLuongSuDung": 100,
    "trangThai": true
  }'
```

## Expected Response Format:

```json
{
  "success": true,
  "message": "Tạo đợt giảm giá thành công",
  "data": {
    "id": 1,
    "maDotGiamGia": "DGG001",
    "tenDotGiamGia": "Đợt giảm giá Black Friday",
    "loaiDotGiamGia": "PHAN_TRAM",
    "giaTriDotGiam": "20",
    "soTien": null,
    "moTa": "Đợt giảm giá lớn nhất trong năm",
    "ngayBatDau": "2024-12-01T00:00:00",
    "ngayKetThuc": "2024-12-31T23:59:59",
    "soLuongSuDung": 1000,
    "trangThai": true
  }
}
```

## Error Response Format:

```json
{
  "success": false,
  "message": "Mã đợt giảm giá đã tồn tại: DGG001",
  "data": null
}
```
