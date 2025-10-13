-- Thêm dữ liệu mẫu cho bảng dia_chi_khach_hang
INSERT INTO dia_chi_khach_hang (id, khach_hang_id, ten_nguoi_nhan, so_dien_thoai, dia_chi, tinh_thanh, quan_huyen, phuong_xa, mac_dinh, trang_thai) VALUES
(1, 1, 'Sample_70', '0934638785', '123 Đường ABC', 'TP.HCM', 'Quận 1', 'Phường Bến Nghé', true, true),
(2, 1, 'Sample_70', '0934638785', '456 Đường XYZ', 'TP.HCM', 'Quận 2', 'Phường Thủ Thiêm', false, true),
(3, 2, 'Sample_67', '0951277399', '789 Đường DEF', 'Hà Nội', 'Quận Ba Đình', 'Phường Phúc Xá', true, true),
(4, 2, 'Sample_67', '0951277399', '321 Đường GHI', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bạc', false, true);

-- Cập nhật sequence cho id
SELECT setval('dia_chi_khach_hang_id_seq', 4, true);
