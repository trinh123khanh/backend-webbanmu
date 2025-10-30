-- Insert sample data cho tất cả các bảng
-- Mỗi bảng 2 bản ghi với các liên kết phù hợp

-- 1. Bảng độc lập không có Foreign Key

-- Nha San Xuat
INSERT INTO nha_san_xuat (ten_nha_san_xuat, quoc_gia, mo_ta, trang_thai) VALUES
('Honda', 'Nhật Bản', 'Hãng sản xuất mũ bảo hiểm nổi tiếng từ Nhật Bản', true),
('LS2', 'Tây Ban Nha', 'Thương hiệu mũ bảo hiểm hàng đầu châu Âu', true);

-- Loai Mu Bao Hiem
INSERT INTO loai_mu_bao_hiem (ten_loai, mo_ta, trang_thai) VALUES
('Mũ Full Face', 'Mũ bảo hiểm che toàn bộ mặt, an toàn nhất', true),
('Mũ Nửa Đầu', 'Mũ bảo hiểm che nửa đầu, thoáng mát', true);

-- Chat Lieu Vo
INSERT INTO chat_lieu_vo (ten_chat_lieu, mo_ta, trang_thai) VALUES
('ABS', 'Nhựa ABS cao cấp, bền chắc', true),
('Polycarbonate', 'Polycarbonate siêu bền, nhẹ', true);

-- Trong Luong
INSERT INTO trong_luong (gia_tri_trong_luong, don_vi, mo_ta, trang_thai) VALUES
(1200.00, 'gram', 'Mũ bảo hiểm nặng 1.2kg', true),
(1350.00, 'gram', 'Mũ bảo hiểm nặng 1.35kg', true);

-- Xuat Xu
INSERT INTO xuat_xu (ten_xuat_xu, mo_ta, trang_thai) VALUES
('Việt Nam', 'Sản xuất tại Việt Nam', true),
('Thái Lan', 'Nhập khẩu từ Thái Lan', true);

-- Kieu Dang Mu
INSERT INTO kieu_dang_mu (ten_kieu_dang, mo_ta, trang_thai) VALUES
('Thể thao', 'Kiểu dáng thể thao năng động', true),
('Cổ điển', 'Kiểu dáng cổ điển, thanh lịch', true);

-- Cong Nghe An Toan
INSERT INTO cong_nghe_an_toan (ten_cong_nghe, mo_ta, trang_thai) VALUES
('DOT', 'Tiêu chuẩn an toàn DOT của Mỹ', true),
('ECE', 'Tiêu chuẩn an toàn ECE của châu Âu', true);

-- Mau Sac
INSERT INTO mau_sac (ten_mau, ma_mau, trang_thai) VALUES
('Đen', '#000000', true),
('Trắng', '#FFFFFF', true);

-- Kich Thuoc
INSERT INTO kich_thuoc (ten_kich_thuoc, mo_ta, trang_thai) VALUES
('M', 'Size M (57-58cm)', true),
('L', 'Size L (59-60cm)', true);

-- Nhan Vien
INSERT INTO nhan_vien (ho_ten, ma_nhan_vien, email, so_dien_thoai, so_can_cuoc_cong_dan, dia_chi, gioi_tinh, ngay_sinh, ngay_vao_lam, trang_thai) VALUES
('Nguyễn Văn A', 'NV001', 'nvana@example.com', '0912345678', '001234567890', '123 Đường ABC, TP.HCM', true, '1990-01-15', '2023-01-01', true),
('Trần Thị B', 'NV002', 'tranthib@example.com', '0987654321', '001234567891', '456 Đường XYZ, Hà Nội', false, '1992-05-20', '2023-03-15', true);

-- Dot Giam Gia
INSERT INTO dot_giam_gia (ma_dot_giam_gia, loai_dot_giam_gia, gia_tri_dot_giam, so_tien, mo_ta, ngay_bat_dau, ngay_ket_thuc, so_luong_su_dung, ten_dot_giam_gia, trang_thai) VALUES
('DGG001', 'Phần trăm', '10', 0, 'Giảm 10% cho tất cả sản phẩm', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1000, 'Khuyến mãi đầu năm', true),
('DGG002', 'Tiền mặt', '50000', 50000, 'Giảm 50.000đ cho đơn hàng từ 500.000đ', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 500, 'Khuyến mãi cuối năm', true);

-- Khach Hang
INSERT INTO khach_hang (ma_khach_hang, ten_khach_hang, email, so_dien_thoai, ngay_sinh, dia_chi, gioi_tinh, diem_tich_luy, ngay_tao, trang_thai, so_lan_mua, lan_mua_gan_nhat) VALUES
('KH001', 'Lê Văn C', 'levanc@example.com', '0901234567', '1985-03-10', '789 Đường DEF, Đà Nẵng', true, 500, '2023-06-01', true, 5, '2024-01-15'),
('KH002', 'Phạm Thị D', 'phamthid@example.com', '0907654321', '1995-07-25', '321 Đường GHI, Cần Thơ', false, 300, '2023-08-10', true, 3, '2024-02-20');

-- 2. Bảng phụ thuộc có Foreign Key

-- San Pham (liên kết với các bảng trên)
INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, loai_mu_bao_hiem_id, nha_san_xuat_id, chat_lieu_vo_id, trong_luong_id, xuat_xu_id, kieu_dang_mu_id, cong_nghe_an_toan_id, mau_sac_id, anh_san_pham, gia_ban, so_luong_ton, ngay_tao, trang_thai) VALUES
('SP001', 'Mũ Bảo Hiểm Honda Full Face', 'Mũ bảo hiểm full face cao cấp của Honda', 1, 1, 1, 1, 1, 1, 1, 1, 'https://example.com/mu1.jpg', 1500000, 50, '2024-01-01', true),
('SP002', 'Mũ Bảo Hiểm LS2 Nửa Đầu', 'Mũ bảo hiểm nửa đầu thời trang của LS2', 2, 2, 2, 2, 2, 2, 2, 2, 'https://example.com/mu2.jpg', 1200000, 30, '2024-01-02', true);

-- Chi Tiet San Pham (liên kết với san_pham, mau_sac, kich_thuoc, trong_luong)
INSERT INTO chi_tiet_san_pham (san_pham_id, mau_sac_id, kich_thuoc_id, trong_luong_id, so_luong_ton, gia_ban, trang_thai) VALUES
(1, 1, 1, 1, '25', '1500000', true),
(2, 2, 2, 2, '15', '1200000', true);

-- Dia Chi Khach Hang (liên kết với khach_hang)
INSERT INTO dia_chi_khach_hang (ten_nguoi_nhan, so_dien_thoai, dia_chi_chi_tiet, tinh_thanh, quan_huyen, phuong_xa, mac_dinh, trang_thai, ngay_tao, ngay_cap_nhat, khach_hang_id) VALUES
('Lê Văn C', '0901234567', '789 Đường DEF', 'Đà Nẵng', 'Hải Châu', 'Phường 1', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('Phạm Thị D', '0907654321', '321 Đường GHI', 'Cần Thơ', 'Ninh Kiều', 'Phường 2', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2);

-- Phieu Giam Gia
INSERT INTO phieu_giam_gia (ma_phieu, ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam, gia_tri_toi_thieu, so_tien_toi_da, hoa_don_toi_thieu, so_luong_dung, ngay_bat_dau, ngay_ket_thuc, trang_thai) VALUES
('PGG001', 'Giảm 20% cho đơn hàng từ 1 triệu', false, 20.00, 1000000.00, 200000.00, 1000000.00, 100, '2024-01-01', '2024-12-31', true),
('PGG002', 'Giảm 100.000đ cho đơn hàng từ 500.000đ', true, 100000.00, 500000.00, 100000.00, 500000.00, 200, '2024-01-01', '2024-12-31', true);

-- Phieu Giam Gia Ca Nhan (liên kết với khach_hang và phieu_giam_gia)
INSERT INTO phieu_giam_gia_ca_nhan (khach_hang_id, phieu_giam_gia_id) VALUES
(1, 1),
(2, 2);

-- Hoa Don (liên kết với khach_hang và nhan_vien)
INSERT INTO hoa_don (ma_hoa_don, khach_hang_id, nhan_vien_id, ngay_tao, ngay_thanh_toan, tong_tien, tien_giam_gia, giam_gia_phan_tram, thanh_tien, ghi_chu, trang_thai, so_luong_san_pham) VALUES
('HD001', 1, 1, '2024-01-15 10:30:00', '2024-01-15 10:35:00', 1500000.00, 300000.00, 20.00, 1200000.00, 'Đơn hàng đầu tiên', 'DA_GIAO_HANG', 1),
('HD002', 2, 2, '2024-02-20 14:20:00', '2024-02-20 14:25:00', 1200000.00, 100000.00, NULL, 1100000.00, 'Đơn hàng thứ hai', 'DANG_GIAO_HANG', 1);

-- Hoa Don Chi Tiet (liên kết với hoa_don và chi_tiet_san_pham)
INSERT INTO hoa_don_chi_tiet (hoa_don_id, chi_tiet_san_pham_id, so_luong, don_gia, giam_gia, thanh_tien) VALUES
(1, 1, 1, 1500000.00, 300000.00, 1200000.00),
(2, 2, 1, 1200000.00, 100000.00, 1100000.00);

-- Chi Tiet Dot Giam Gia (liên kết với dot_giam_gia và san_pham)
INSERT INTO chi_tiet_dot_giam_gia (dot_giam_gia_id, san_pham_id, phan_tram_giam, trang_thai) VALUES
(1, 1, 10.00, true),
(2, 2, 5.00, true);
