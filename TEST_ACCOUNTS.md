# 🔐 TÀI KHOẢN TEST

Các tài khoản test được tự động tạo khi backend khởi động (chỉ trong môi trường development).

## 📋 Danh sách tài khoản

### 👨‍💼 ADMIN (Quản trị viên)
- **Username:** `admin`
- **Password:** `admin123`
- **Email:** `admin@tdkstore.com`
- **Full Name:** Quản Trị Viên
- **Quyền:** Toàn quyền hệ thống

### 👨‍💻 STAFF (Nhân viên)
- **Username:** `staff`
- **Password:** `staff123`
- **Email:** `staff@tdkstore.com`
- **Full Name:** Nhân Viên
- **Quyền:** Xử lý hóa đơn tại quầy, xem/sửa hóa đơn do mình tạo

### 🧑 CUSTOMER 1 (Khách hàng)
- **Username:** `customer1`
- **Password:** `customer123`
- **Email:** `customer1@tdkstore.com`
- **Full Name:** Khách Hàng 1
- **Quyền:** Xem và mua hàng trên website, xem đơn hàng của mình

### 🧑 CUSTOMER 2 (Khách hàng)
- **Username:** `customer2`
- **Password:** `customer123`
- **Email:** `customer2@tdkstore.com`
- **Full Name:** Khách Hàng 2
- **Quyền:** Xem và mua hàng trên website, xem đơn hàng của mình

## 🚀 Cách sử dụng

1. **Khởi động backend:**
   ```bash
   cd backend-webbanmu
   ./gradlew bootRun
   ```
cd duanbanmu
npm start
2. **Kiểm tra log console:**
   - Khi backend khởi động, bạn sẽ thấy log:
   ```
   ✅ Đã tạo user: admin (Role: ADMIN)
   ✅ Đã tạo user: staff (Role: STAFF)
   ✅ Đã tạo user: customer1 (Role: CUSTOMER)
   ✅ Đã tạo user: customer2 (Role: CUSTOMER)
   ```

3. **Đăng nhập trên frontend:**
   - Vào `http://localhost:4200/login`
   - Nhập username và password từ danh sách trên

## 🔒 Lưu ý bảo mật

- ⚠️ **CHỈ DÙNG TRONG MÔI TRƯỜNG DEVELOPMENT**
- Các tài khoản này chỉ được tạo tự động trong môi trường development
- Trong production, TestDataInitializer sẽ tự động bỏ qua
- Không sử dụng các mật khẩu này trong môi trường production!

## 🔄 Reset tài khoản

Để xóa và tạo lại các tài khoản test:

1. Xóa các bản ghi trong bảng `users`:
   ```sql
   DELETE FROM users WHERE username IN ('admin', 'staff', 'customer1', 'customer2');
   ```

2. Khởi động lại backend, các tài khoản sẽ được tạo lại tự động.

## 📝 Test các tính năng

### Với tài khoản ADMIN:
- ✅ Xem thống kê
- ✅ CRUD tất cả hóa đơn
- ✅ Quản lý sản phẩm
- ✅ Quản lý tài khoản (nhân viên & khách hàng)
- ✅ Quản lý giảm giá

### Với tài khoản STAFF:
- ✅ Xem hóa đơn (chỉ của mình tạo)
- ✅ Bán tại quầy
- ❌ Không thể xem thống kê
- ❌ Không thể quản lý sản phẩm

### Với tài khoản CUSTOMER:
- ✅ Xem và mua hàng trên shop website
- ✅ Xem đơn hàng của mình
- ✅ Hủy đơn hàng (nếu ở trạng thái Chờ xác nhận)
- ❌ Không thể truy cập admin panel

