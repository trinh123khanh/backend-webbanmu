-- Script để tạo dữ liệu mẫu cho bảng hóa đơn
-- Chạy script này trong database để có dữ liệu test

-- Tạo dữ liệu mẫu cho bảng khach_hang
INSERT INTO khach_hang (id, ten_khach_hang, email, so_dien_thoai, ngay_sinh, gioi_tinh, diem_tich_luy, ngay_tao, trang_thai) VALUES
(1, 'Nguyễn Văn An', 'an.nguyen@email.com', '0123456789', '1990-01-15', true, 100, '2024-01-01', true),
(2, 'Trần Thị Bình', 'binh.tran@email.com', '0987654321', '1985-05-20', false, 200, '2024-01-02', true),
(3, 'Lê Văn Cường', 'cuong.le@email.com', '0369258147', '1992-08-10', true, 150, '2024-01-03', true);

-- Tạo dữ liệu mẫu cho bảng nhan_vien
INSERT INTO nhan_vien (id, ho_ten, email, so_dien_thoai, dia_chi, gioi_tinh, ngay_sinh, ngay_vao_lam, trang_thai) VALUES
(1, 'Nguyễn Văn A', 'nva@company.com', '0123456780', 'Hà Nội', true, '1988-03-15', '2020-01-01', true),
(2, 'Trần Thị B', 'ttb@company.com', '0987654320', 'TP.HCM', false, '1990-07-22', '2020-02-01', true);

-- Tạo dữ liệu mẫu cho bảng hoa_don
INSERT INTO hoa_don (id, ma_hoa_don, khach_hang_id, nhan_vien_id, ngay_tao, ngay_thanh_toan, tong_tien, tien_giam_gia, thanh_tien, ghi_chu, trang_thai) VALUES
(1, 'HD20241201001', 1, 1, '2024-12-01 10:30:00', '2024-12-01 11:00:00', 3000000, 150000, 2850000, 'Giao hàng tận nơi', 'DA_GIAO_HANG'),
(2, 'HD20241201002', 2, 2, '2024-12-01 14:20:00', '2024-12-01 14:30:00', 2500000, 0, 2500000, '', 'DA_XAC_NHAN'),
(3, 'HD20241201003', 3, 1, '2024-12-01 16:45:00', NULL, 3200000, 320000, 2880000, 'Khách hàng VIP', 'CHO_XAC_NHAN'),
(4, 'HD20241202001', 1, 2, '2024-12-02 09:15:00', '2024-12-02 09:30:00', 1500000, 75000, 1425000, 'Đơn hàng nhỏ', 'DA_GIAO_HANG'),
(5, 'HD20241202002', 2, 1, '2024-12-02 11:30:00', NULL, 4500000, 225000, 4275000, 'Đơn hàng lớn', 'DANG_GIAO_HANG');

-- Cập nhật sequence nếu cần
-- ALTER SEQUENCE hoa_don_id_seq RESTART WITH 6;
-- ALTER SEQUENCE khach_hang_id_seq RESTART WITH 4;
-- ALTER SEQUENCE nhan_vien_id_seq RESTART WITH 3;
