# Logic Trừ Số Lượng Sản Phẩm Khi Mua Hàng

## 📊 Bảng và Cột Được Cập Nhật

### Bảng: `chi_tiet_san_pham`
### Cột: `so_luong_ton` (kiểu dữ liệu: VARCHAR/TEXT - lưu dưới dạng String)

**Entity**: `ChiTietSanPham.java`
```java
@Column(nullable = false)
private String soLuongTon;
```

**Mối quan hệ**:
- Bảng `chi_tiet_san_pham` chứa thông tin chi tiết sản phẩm (màu sắc, kích thước, trọng lượng)
- Mỗi sản phẩm (`san_pham`) có thể có nhiều chi tiết sản phẩm (`chi_tiet_san_pham`)
- Số lượng tồn kho được quản lý ở cấp `chi_tiet_san_pham`, không phải ở cấp `san_pham`

---

## 🔄 Các Trường Hợp Trừ Số Lượng

### **QUAN TRỌNG: Chỉ Trừ Số Lượng Khi Thanh Toán Thành Công**

**File**: `HoaDonService.java` → `updateTrangThaiHoaDon()`

**Điều kiện**:
- Trạng thái hoá đơn chuyển **SANG** `DA_XAC_NHAN` (Đã xác nhận)
- Từ bất kỳ trạng thái nào khác (trừ `DA_XAC_NHAN`)
- **Áp dụng cho CẢ đơn hàng online và đơn hàng tại quầy**

**Logic**:
```java
if (newTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN && 
    oldTrangThai != HoaDon.TrangThaiHoaDon.DA_XAC_NHAN) {
    // Trừ số lượng khi xác nhận đơn hàng (khách hàng đã thanh toán thành công)
    deductStockFromInvoice(chiTietBeforeUpdate);
}
```

**Thời điểm**: Khi admin/staff cập nhật trạng thái hoá đơn thành "Đã xác nhận" (dòng 703-707)

**Lưu ý**:
- Khi tạo hoá đơn từ checkout (online) với status = `CHO_XAC_NHAN`, **KHÔNG trừ số lượng**
- Số lượng chỉ được trừ khi admin/staff xác nhận đơn hàng (status = `DA_XAC_NHAN`)
- Điều này đảm bảo số lượng chỉ bị trừ khi khách hàng thực sự đã thanh toán thành công

---

## 📦 Hàm Trừ Số Lượng: `deductStockFromInvoice()`

**File**: `HoaDonService.java` (dòng 775-833)

**Quy trình**:
1. Duyệt qua từng `HoaDonChiTiet` trong danh sách
2. Lấy `chiTietSanPhamId` từ mỗi chi tiết
3. Load lại `ChiTietSanPham` từ database để đảm bảo dữ liệu mới nhất
4. Tính toán số lượng mới: `newStock = currentStock - requestedQuantity`
5. Cập nhật vào database:
   ```java
   chiTietSanPham.setSoLuongTon(String.valueOf(newStock));
   chiTietSanPhamRepository.save(chiTietSanPham);
   chiTietSanPhamRepository.flush();
   ```

**Lưu ý**:
- Nếu `newStock < 0`, sẽ đặt về `0` (không cho phép số lượng âm)
- Log chi tiết từng bước để theo dõi

---

## 🔄 Hàm Hoàn Lại Số Lượng: `restoreStockFromInvoice()`

**File**: `HoaDonService.java` (dòng 839-890)

**Điều kiện**:
- Hoá đơn chuyển **TỪ** `DA_XAC_NHAN` **SANG** trạng thái khác
- **TRỪ** các trạng thái: `DA_HUY`, `DA_GIAO_HANG`, `DANG_GIAO_HANG` (không hoàn lại vì đơn đang tiến triển)

**Logic**:
```java
else if (oldTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN && 
         newTrangThai != HoaDon.TrangThaiHoaDon.DA_XAC_NHAN &&
         newTrangThai != HoaDon.TrangThaiHoaDon.DA_HUY &&
         newTrangThai != HoaDon.TrangThaiHoaDon.DA_GIAO_HANG &&
         newTrangThai != HoaDon.TrangThaiHoaDon.DANG_GIAO_HANG) {
    // Hoàn lại số lượng (ví dụ: hủy xác nhận)
    restoreStockFromInvoice(chiTietBeforeUpdate);
}
```

**Quy trình**:
1. Duyệt qua từng `HoaDonChiTiet`
2. Tính toán số lượng mới: `newStock = currentStock + quantityToRestore`
3. Cập nhật vào database

---

## ⚠️ QUAN TRỌNG: Giỏ Hàng KHÔNG Trừ Số Lượng

**File**: `HoaDonChoService.java`

**Các thao tác KHÔNG trừ số lượng**:
- ❌ Thêm sản phẩm vào giỏ hàng (`addItemToCart`) - chỉ kiểm tra tồn kho
- ❌ Cập nhật số lượng trong giỏ hàng (`updateCartItemQuantity`) - chỉ kiểm tra tồn kho
- ❌ Xóa sản phẩm khỏi giỏ hàng (`removeItemFromCart`) - không hoàn lại vì chưa trừ

**Lý do**: Số lượng chỉ được trừ khi **thanh toán thành công** (tạo hoá đơn), không phải khi thêm vào giỏ hàng.

---

## 📋 Tóm Tắt

| Trường Hợp | Thời Điểm | Bảng | Cột | Method |
|-----------|-----------|------|-----|--------|
| **Tất cả đơn hàng** (Online + Tại quầy) | Khi cập nhật status = DA_XAC_NHAN (xác nhận thanh toán) | `chi_tiet_san_pham` | `so_luong_ton` | `updateTrangThaiHoaDon()` → `deductStockFromInvoice()` |
| Hoàn Lại | Khi hủy xác nhận (từ DA_XAC_NHAN sang trạng thái khác) | `chi_tiet_san_pham` | `so_luong_ton` | `updateTrangThaiHoaDon()` → `restoreStockFromInvoice()` |

**Lưu ý**: Khi tạo hoá đơn (cả online và tại quầy), **KHÔNG trừ số lượng**. Số lượng chỉ được trừ khi admin/staff xác nhận đơn hàng (status = `DA_XAC_NHAN`).

---

## 🔍 Kiểm Tra Database

### Query để xem số lượng tồn kho:
```sql
SELECT 
    csp.id,
    csp.so_luong_ton,
    sp.ten_san_pham,
    ms.ten_mau AS mau_sac,
    kt.ten_kich_thuoc AS kich_thuoc
FROM chi_tiet_san_pham csp
JOIN san_pham sp ON csp.san_pham_id = sp.id
LEFT JOIN mau_sac ms ON csp.mau_sac_id = ms.id
LEFT JOIN kich_thuoc kt ON csp.kich_thuoc_id = kt.id
ORDER BY csp.id;
```

### Query để xem lịch sử trừ số lượng (từ hoá đơn):
```sql
SELECT 
    hd.ma_hoa_don,
    hd.trang_thai,
    hdct.so_luong,
    csp.id AS chi_tiet_san_pham_id,
    csp.so_luong_ton AS so_luong_hien_tai,
    sp.ten_san_pham
FROM hoa_don hd
JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.hoa_don_id
JOIN chi_tiet_san_pham csp ON hdct.chi_tiet_san_pham_id = csp.id
JOIN san_pham sp ON csp.san_pham_id = sp.id
WHERE hd.trang_thai IN ('CHO_XAC_NHAN', 'DA_XAC_NHAN')
ORDER BY hd.ngay_tao DESC;
```

---

## 📝 Logs và Debug

Khi trừ số lượng, hệ thống sẽ log:
```
📦 Deducting stock for X items...
✅ Deducted stock for ChiTietSanPham id: XXX - Y units (from Z to W)
✅ Stock deduction completed
```

Kiểm tra logs trong `application.log` để theo dõi quá trình trừ số lượng.

