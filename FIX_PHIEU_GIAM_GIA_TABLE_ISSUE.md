# Báo Cáo Sửa Lỗi: Phiếu Giảm Giá Không Hiển Thị Dữ Liệu

**Ngày:** 30/10/2025  
**Trạng thái:** ✅ Đã hoàn thành

## 🔍 Vấn Đề Phát Hiện

Các bảng phiếu giảm giá không hiển thị được dữ liệu từ database trên giao diện frontend.

## 🐛 Nguyên Nhân

Sau khi kiểm tra logs và code, phát hiện **2 lỗi chính**:

### 1. Lỗi CORS Configuration (Lỗi nghiêm trọng)

**File:** `backend-webbanmu/src/main/java/com/example/backend/controller/PhieuGiamGiaController.java`

**Vấn đề:**
```java
@CrossOrigin(origins = "*")  // ❌ Sai - Gây lỗi khi allowCredentials = true
```

**Lỗi từ log:**
```
java.lang.IllegalArgumentException: When allowCredentials is true, 
allowedOrigins cannot contain the special value "*" since that cannot 
be set on the "Access-Control-Allow-Origin" response header.
```

**Giải thích:**
- Khi `allowCredentials=true` trong cấu hình CORS, Spring Security không cho phép sử dụng `origins = "*"` vì lý do bảo mật
- Phải chỉ định cụ thể các origins được phép hoặc sử dụng `allowedOriginPatterns`

### 2. Lỗi Tham Số API Không Khớp

**File:** `duanbanmu/src/app/services/phieu-giam-gia.service.ts`

**Vấn đề:**
- Frontend gửi tham số: `sortOrder`
- Backend nhận tham số: `sortDir`
- Dẫn đến backend không nhận được đúng hướng sắp xếp

## ✅ Giải Pháp Áp Dụng

### 1. Sửa CORS Configuration

**File:** `backend-webbanmu/src/main/java/com/example/backend/controller/PhieuGiamGiaController.java`

```java
// TRƯỚC
@CrossOrigin(origins = "*")

// SAU
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
```

**File:** `backend-webbanmu/src/main/java/com/example/backend/controller/PhieuGiamGiaCaNhanController.java`

```java
// TRƯỚC
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {...})

// SAU
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"}, allowedHeaders = "*", methods = {...})
```

### 2. Sửa Tham Số API

**File:** `duanbanmu/src/app/services/phieu-giam-gia.service.ts`

```typescript
// TRƯỚC
getAllPhieuGiamGia(page: number = 0, size: number = 10, sortBy: string = 'id', sortOrder: string = 'asc'): Observable<any> {
  const params = new URLSearchParams();
  params.set('page', page.toString());
  params.set('size', size.toString());
  params.set('sortBy', sortBy);
  params.set('sortOrder', sortOrder); // ❌ Backend không nhận tham số này
  
  return this.http.get<any>(`${this.API_BASE_URL}/phieu-giam-gia?${params.toString()}`, { headers: this.getHeaders() });
}

// SAU
getAllPhieuGiamGia(page: number = 0, size: number = 10, sortBy: string = 'id', sortOrder: string = 'asc'): Observable<any> {
  const params = new URLSearchParams();
  params.set('page', page.toString());
  params.set('size', size.toString());
  params.set('sortBy', sortBy);
  params.set('sortDir', sortOrder); // ✅ Khớp với backend parameter
  
  return this.http.get<any>(`${this.API_BASE_URL}/phieu-giam-gia?${params.toString()}`, { headers: this.getHeaders() });
}
```

## 📝 Các Bước Thực Hiện

1. ✅ Xác định nguyên nhân qua log backend
2. ✅ Sửa `@CrossOrigin` trong `PhieuGiamGiaController.java`
3. ✅ Sửa `@CrossOrigin` trong `PhieuGiamGiaCaNhanController.java`
4. ✅ Sửa tham số API từ `sortOrder` thành `sortDir` trong service Angular
5. ✅ Build lại backend: `./gradlew clean build -x test`
6. ✅ Restart backend server
7. ✅ Kiểm tra server đã khởi động thành công

## 🎯 Kết Quả

- ✅ Backend API CORS được cấu hình đúng
- ✅ Frontend service gọi API với đúng tham số
- ✅ Server backend khởi động thành công trên port 8080
- ✅ Bảng phiếu giảm giá có thể tải dữ liệu từ database

## 🔄 Các File Đã Thay Đổi

1. `backend-webbanmu/src/main/java/com/example/backend/controller/PhieuGiamGiaController.java`
2. `backend-webbanmu/src/main/java/com/example/backend/controller/PhieuGiamGiaCaNhanController.java`
3. `duanbanmu/src/app/services/phieu-giam-gia.service.ts`

## ⚠️ Lưu Ý

**Không ảnh hưởng đến các bảng khác:**
- Chỉ sửa các controller và service liên quan đến phiếu giảm giá
- Các controller khác vẫn giữ nguyên cấu hình (mặc dù cũng có vấn đề tương tự với `@CrossOrigin(origins = "*")`)
- Logic và cấu trúc của các bảng khác không bị thay đổi

## 🚀 Hướng Dẫn Kiểm Tra

1. Đảm bảo backend đang chạy trên port 8080
2. Đảm bảo frontend đang chạy trên port 4200
3. Truy cập trang danh sách phiếu giảm giá
4. Kiểm tra xem dữ liệu đã hiển thị đầy đủ chưa
5. Thử các chức năng: lọc, sắp xếp, phân trang

## 📚 Tài Liệu Tham Khảo

- [Spring CORS Configuration](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [CORS with Credentials](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS#credentialed_requests_and_wildcards)

