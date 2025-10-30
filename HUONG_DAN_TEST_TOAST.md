# 🧪 HƯỚNG DẪN TEST TOAST NOTIFICATION

**Ngày tạo:** 29/10/2025  
**Trạng thái:** ✅ Đã thêm nút test vào form

---

## 🚀 CÁCH TEST NGAY

### Bước 1: Khởi động ứng dụng

```powershell
# Nếu chưa chạy backend
cd C:\Users\Thang\Documents\DATN\backend-webbanmu
.\START_SERVER.ps1

# Nếu chưa chạy frontend (terminal mới)
cd C:\Users\Thang\Documents\DATN\duanbanmu
npm start
```

### Bước 2: Truy cập trang tạo phiếu giảm giá

```
http://localhost:4200/phieu-giam-gia/create
```

### Bước 3: Test Toast với nút test

Ở cuối form, bạn sẽ thấy 2 nút test màu xanh và đỏ:

```
┌──────────────┬──────────────┬─────────────────────────┬────────────────────┐
│ Thêm mới     │ Hủy          │ 🧪 Test Toast Thành công│ 🧪 Test Toast Lỗi  │
└──────────────┴──────────────┴─────────────────────────┴────────────────────┘
```

**Click vào từng nút:**
1. **🧪 Test Toast Thành công** (màu xanh lá) → Toast xanh sẽ hiện góc trên phải
2. **🧪 Test Toast Lỗi** (màu đỏ) → Toast đỏ sẽ hiện góc trên phải

---

## 📊 KIỂM TRA CONSOLE LOG

Mở Developer Tools (F12) → Console tab

Bạn sẽ thấy log như sau:

```
🧪 Testing success toast...
✅ showSuccessMessage called: Đây là thông báo thành công để test! Toast notification đang hoạt động tốt. 🎉
✅ successMessage set to: Đây là thông báo thành công để test! Toast notification đang hoạt động tốt. 🎉
```

Hoặc với error:

```
🧪 Testing error toast...
❌ showErrorMessage called: Đây là thông báo lỗi để test! Toast notification đang hoạt động tốt. ⚠️
❌ errorMessage set to: Đây là thông báo lỗi để test! Toast notification đang hoạt động tốt. ⚠️
```

---

## 🎯 KIỂM TRA TOAST THẬT

### Test 1: Validation Error
1. Để trống tất cả các field
2. Click "Thêm mới"
3. **Kết quả mong đợi:**
   - Toast đỏ hiện ra: "Vui lòng kiểm tra lại thông tin nhập vào!"
   - Các field có lỗi hiển thị thông báo lỗi dưới mỗi input

### Test 2: Tạo Phiếu Giảm Giá Thành Công (Công khai)
1. Điền đầy đủ thông tin:
   - Click nút "💡" để tạo mã tự động
   - Tên phiếu: "Test Toast Notification"
   - Loại: Tiền mặt
   - Giá trị giảm: 50000
   - Hóa đơn tối thiểu: 100000
   - Số lượng: 10
   - Chọn ngày bắt đầu và kết thúc
   - **Trạng thái: Công khai**
2. Click "Thêm mới"
3. **Kết quả mong đợi:**
   - Toast xanh hiện ra: "Tạo phiếu giảm giá công khai thành công!"
   - Sau 2 giây chuyển về trang list

### Test 3: Tạo Phiếu Giảm Giá Cá Nhân (với Email)
1. Điền đầy đủ thông tin như Test 2
2. **Trạng thái: Cá nhân**
3. Chọn ít nhất 1 khách hàng có email
4. Click "Thêm mới"
5. **Kết quả mong đợi:**
   - Toast xanh hiện ra: "Tạo phiếu giảm giá cá nhân thành công cho X khách hàng! Email thông báo đang được gửi."
   - Email được gửi đến khách hàng (kiểm tra log backend)
   - Sau 2 giây chuyển về trang list

### Test 4: Lỗi Backend (Mã trùng)
1. Tạo phiếu giảm giá với mã "TEST001"
2. Tạo lại phiếu giảm giá với cùng mã "TEST001"
3. **Kết quả mong đợi:**
   - Toast đỏ hiện ra: "Mã phiếu giảm giá đã tồn tại: TEST001"

---

## 🔍 TROUBLESHOOTING (Nếu không thấy Toast)

### Vấn đề 1: Không thấy Toast xuất hiện

**Nguyên nhân có thể:**
- CSS chưa load
- Z-index bị che
- Bootstrap Icons chưa load

**Giải pháp:**
1. **Hard refresh:** Ctrl + Shift + R (hoặc Ctrl + F5)
2. **Xóa cache:** 
   - Chrome: F12 → Network tab → Disable cache (checkbox)
   - Hoặc: Settings → Privacy → Clear browsing data
3. **Kiểm tra Console:**
   - F12 → Console tab
   - Xem có lỗi CSS/JS không
4. **Kiểm tra Elements:**
   - F12 → Elements tab
   - Tìm class `.toast-container`
   - Kiểm tra style của toast

### Vấn đề 2: Toast hiện nhưng không có icon

**Nguyên nhân:**
- Bootstrap Icons chưa load

**Giải pháp:**
```powershell
# Cài lại Bootstrap Icons
cd duanbanmu
npm install bootstrap-icons --save
npm start
```

### Vấn đề 3: Toast hiện quá nhanh/chậm

**Điều chỉnh thời gian:**

Mở file: `duanbanmu/src/app/components/phieu-giam-gia-form/phieu-giam-gia-form.component.ts`

Tìm dòng:
```typescript
}, 5000); // Auto hide after 5 seconds
```

Thay đổi:
- `3000` = 3 giây
- `7000` = 7 giây
- `10000` = 10 giây

### Vấn đề 4: Toast bị che bởi header/sidebar

**Điều chỉnh vị trí:**

Mở file: `duanbanmu/src/app/components/phieu-giam-gia-form/phieu-giam-gia-form.component.scss`

Tìm:
```scss
.toast-container {
  position: fixed;
  top: 80px;      // ← Thay đổi giá trị này
  right: 20px;
  z-index: 99999 !important;
}
```

Thử các giá trị:
- `top: 100px;` - Thấp hơn
- `top: 60px;` - Cao hơn
- `bottom: 20px;` - Hiển thị dưới cùng

---

## 📸 KIỂM TRA TRỰC QUAN

### Toast Success (Xanh lá)
```
┌─────────────────────────────────────────┐
│ ✓  Thành công!                     [X]  │
│    Tạo phiếu giảm giá công khai thành   │
│    công!                                │
└─────────────────────────────────────────┘
↑ Màu viền trái: Xanh lá (#28a745)
```

### Toast Error (Đỏ)
```
┌─────────────────────────────────────────┐
│ ⚠  Lỗi!                            [X]  │
│    Vui lòng kiểm tra lại thông tin nhập │
│    vào!                                 │
└─────────────────────────────────────────┘
↑ Màu viền trái: Đỏ (#dc3545)
```

---

## 🗑️ XÓA CÁC NÚT TEST (SAU KHI HOÀN THÀNH)

Khi đã kiểm tra xong và toast hoạt động tốt, bạn có thể xóa 2 nút test:

### Cách 1: Ẩn bằng CSS (Khuyến nghị)

Mở: `duanbanmu/src/app/components/phieu-giam-gia-form/phieu-giam-gia-form.component.scss`

Thêm vào cuối file:
```scss
// Hide test buttons in production
.btn-success[style*="margin-left: auto"],
.btn-danger:has(+ .btn-success) {
  display: none !important;
}
```

### Cách 2: Xóa khỏi HTML

Mở: `duanbanmu/src/app/components/phieu-giam-gia-form/phieu-giam-gia-form.component.html`

Xóa dòng 267-280:
```html
<!-- Test Toast Buttons (tạm thời để test) -->
<button 
  type="button"
  class="btn btn-success"
  (click)="testSuccessToast()"
  style="margin-left: auto;">
  🧪 Test Toast Thành công
</button>
<button 
  type="button"
  class="btn btn-danger"
  (click)="testErrorToast()">
  🧪 Test Toast Lỗi
</button>
```

### Cách 3: Xóa methods khỏi TypeScript

Mở: `duanbanmu/src/app/components/phieu-giam-gia-form/phieu-giam-gia-form.component.ts`

Xóa dòng 886-896:
```typescript
// Test methods (tạm thời để test toast)
testSuccessToast() {
  console.log('🧪 Testing success toast...');
  this.showSuccessMessage('Đây là thông báo thành công để test! Toast notification đang hoạt động tốt. 🎉');
}

testErrorToast() {
  console.log('🧪 Testing error toast...');
  this.showErrorMessage('Đây là thông báo lỗi để test! Toast notification đang hoạt động tốt. ⚠️');
}
```

---

## ✅ CHECKLIST KIỂM TRA

- [ ] Toast success hiển thị khi click nút test xanh
- [ ] Toast error hiển thị khi click nút test đỏ
- [ ] Toast tự động ẩn sau 5 giây
- [ ] Click nút X để đóng toast hoạt động
- [ ] Console log hiển thị đúng message
- [ ] Toast hiển thị khi validation lỗi
- [ ] Toast hiển thị khi tạo phiếu thành công
- [ ] Toast hiển thị khi có lỗi từ backend
- [ ] Toast responsive trên mobile
- [ ] Icon hiển thị đúng (✓ và ⚠)

---

## 📞 HỖ TRỢ

Nếu vẫn gặp vấn đề:
1. Kiểm tra Console log (F12)
2. Kiểm tra Network tab (F12) - xem có lỗi load CSS/JS không
3. Hard refresh (Ctrl + Shift + R)
4. Restart cả backend và frontend
5. Xóa node_modules và cài lại: `npm install`

**Ngày cập nhật:** 29/10/2025  
**Version:** 1.0  
**Người tạo:** AI Assistant

