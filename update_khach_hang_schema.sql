-- Script để cập nhật cấu trúc bảng khach_hang
-- Thêm 2 cột mới: so_lan_mua và lan_mua_gan_nhat

-- Thêm cột so_lan_mua (số lần mua)
ALTER TABLE khach_hang 
ADD COLUMN so_lan_mua INT DEFAULT 0;

-- Thêm cột lan_mua_gan_nhat (lần mua gần nhất)
ALTER TABLE khach_hang 
ADD COLUMN lan_mua_gan_nhat DATE DEFAULT NULL;

-- Cập nhật dữ liệu cho các khách hàng hiện có
-- Đếm số lần mua từ bảng hoa_don và cập nhật ngày mua gần nhất
UPDATE khach_hang kh 
SET 
    so_lan_mua = (
        SELECT COUNT(*) 
        FROM hoa_don hd 
        WHERE hd.khach_hang_id = kh.id 
        AND hd.trang_thai IN ('DA_GIAO_HANG', 'DA_XAC_NHAN', 'DANG_GIAO_HANG')
    ),
    lan_mua_gan_nhat = (
        SELECT MAX(hd.ngay_tao) 
        FROM hoa_don hd 
        WHERE hd.khach_hang_id = kh.id 
        AND hd.trang_thai IN ('DA_GIAO_HANG', 'DA_XAC_NHAN', 'DANG_GIAO_HANG')
    )
WHERE EXISTS (
    SELECT 1 
    FROM hoa_don hd 
    WHERE hd.khach_hang_id = kh.id
);

-- Thêm comment cho các cột mới
ALTER TABLE khach_hang 
MODIFY COLUMN so_lan_mua INT DEFAULT 0 COMMENT 'Số lần mua hàng của khách hàng';

ALTER TABLE khach_hang 
MODIFY COLUMN lan_mua_gan_nhat DATE DEFAULT NULL COMMENT 'Ngày mua hàng gần nhất của khách hàng';

-- Tạo trigger để tự động cập nhật số lần mua và ngày mua gần nhất khi có hóa đơn mới
DELIMITER $$

CREATE TRIGGER tr_update_khach_hang_purchase_stats_after_insert
AFTER INSERT ON hoa_don
FOR EACH ROW
BEGIN
    UPDATE khach_hang 
    SET 
        so_lan_mua = (
            SELECT COUNT(*) 
            FROM hoa_don 
            WHERE khach_hang_id = NEW.khach_hang_id 
            AND trang_thai IN ('DA_GIAO_HANG', 'DA_XAC_NHAN', 'DANG_GIAO_HANG')
        ),
        lan_mua_gan_nhat = (
            SELECT MAX(ngay_tao) 
            FROM hoa_don 
            WHERE khach_hang_id = NEW.khach_hang_id 
            AND trang_thai IN ('DA_GIAO_HANG', 'DA_XAC_NHAN', 'DANG_GIAO_HANG')
        )
    WHERE id = NEW.khach_hang_id;
END$$

CREATE TRIGGER tr_update_khach_hang_purchase_stats_after_update
AFTER UPDATE ON hoa_don
FOR EACH ROW
BEGIN
    -- Chỉ cập nhật khi trạng thái hóa đơn thay đổi
    IF OLD.trang_thai != NEW.trang_thai THEN
        UPDATE khach_hang 
        SET 
            so_lan_mua = (
                SELECT COUNT(*) 
                FROM hoa_don 
                WHERE khach_hang_id = NEW.khach_hang_id 
                AND trang_thai IN ('DA_GIAO_HANG', 'DA_XAC_NHAN', 'DANG_GIAO_HANG')
            ),
            lan_mua_gan_nhat = (
                SELECT MAX(ngay_tao) 
                FROM hoa_don 
                WHERE khach_hang_id = NEW.khach_hang_id 
                AND trang_thai IN ('DA_GIAO_HANG', 'DA_XAC_NHAN', 'DANG_GIAO_HANG')
            )
        WHERE id = NEW.khach_hang_id;
    END IF;
END$$

DELIMITER ;

-- Kiểm tra kết quả
SELECT 
    id,
    ten_khach_hang,
    so_lan_mua,
    lan_mua_gan_nhat
FROM khach_hang 
ORDER BY lan_mua_gan_nhat DESC 
LIMIT 10;
