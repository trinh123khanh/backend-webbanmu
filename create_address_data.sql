-- Script để tạo dữ liệu mẫu trong bảng dia_chi_khach_hang
-- Chạy script này trực tiếp trong database

-- Xóa dữ liệu cũ nếu có
DELETE FROM dia_chi_khach_hang WHERE id IN (1, 2, 3, 4);

-- Tạo dữ liệu mẫu
INSERT INTO dia_chi_khach_hang (id, khach_hang_id, ten_nguoi_nhan, so_dien_thoai, dia_chi, tinh_thanh, quan_huyen, phuong_xa, mac_dinh, trang_thai) VALUES
(1, 1, 'Sample_70', '0934638785', '123 Đường Nguyễn Huệ', 'TP.HCM', 'Quận 1', 'Phường Bến Nghé', true, true),
(2, 1, 'Sample_70', '0934638785', '456 Đường Lê Lợi', 'TP.HCM', 'Quận 3', 'Phường Bến Nghé', false, true),
(3, 2, 'Sample_67', '0951277399', '789 Đường Trần Hưng Đạo', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bạc', true, true),
(4, 2, 'Sample_67', '0951277399', '321 Đường Lý Thái Tổ', 'Hà Nội', 'Quận Ba Đình', 'Phường Phúc Xá', false, true);

-- Cập nhật sequence
SELECT setval('dia_chi_khach_hang_id_seq', 4, true);
