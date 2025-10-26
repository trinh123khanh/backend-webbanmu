-- Script để cập nhật cấu trúc bảng khach_hang
-- Đổi cột ma_khach_hang từ INT sang VARCHAR

-- Bước 1: Backup dữ liệu hiện tại (tùy chọn)
-- CREATE TABLE khach_hang_backup AS SELECT * FROM khach_hang;

-- Bước 2: Thêm cột tạm thời với kiểu VARCHAR
ALTER TABLE khach_hang 
ADD COLUMN ma_khach_hang_temp VARCHAR(50);

-- Bước 3: Chuyển đổi dữ liệu từ INT sang VARCHAR
UPDATE khach_hang 
SET ma_khach_hang_temp = CAST(ma_khach_hang AS CHAR);

-- Bước 4: Xóa cột cũ
ALTER TABLE khach_hang 
DROP COLUMN ma_khach_hang;

-- Bước 5: Đổi tên cột tạm thời thành tên cũ
ALTER TABLE khach_hang 
CHANGE COLUMN ma_khach_hang_temp ma_khach_hang VARCHAR(50);

-- Bước 6: Thêm comment cho cột
ALTER TABLE khach_hang 
MODIFY COLUMN ma_khach_hang VARCHAR(50) COMMENT 'Mã khách hàng (dạng chuỗi)';

-- Bước 7: Tạo index cho cột ma_khach_hang để tối ưu tìm kiếm
CREATE INDEX idx_ma_khach_hang ON khach_hang(ma_khach_hang);

-- Bước 8: Cập nhật dữ liệu mẫu (nếu cần)
-- Ví dụ: Cập nhật mã khách hàng theo format mới
UPDATE khach_hang 
SET ma_khach_hang = CONCAT('KH', LPAD(id, 6, '0'))
WHERE ma_khach_hang IS NULL OR ma_khach_hang = '';

-- Bước 9: Kiểm tra kết quả
SELECT 
    id,
    ma_khach_hang,
    ten_khach_hang,
    email
FROM khach_hang 
ORDER BY id 
LIMIT 10;

-- Bước 10: Kiểm tra cấu trúc bảng
DESCRIBE khach_hang;
