-- Cập nhật bảng phieu_giam_gia_ca_nhan chỉ có 3 trường
-- Xóa bảng cũ và tạo lại với schema đơn giản

-- Xóa bảng cũ
DROP TABLE IF EXISTS phieu_giam_gia_ca_nhan CASCADE;

-- Tạo lại bảng với chỉ 3 trường
CREATE TABLE phieu_giam_gia_ca_nhan (
    id BIGSERIAL PRIMARY KEY,
    khach_hang_id BIGINT NOT NULL,
    phieu_giam_gia_id BIGINT NOT NULL,
    FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id) ON DELETE CASCADE,
    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE,
    UNIQUE(phieu_giam_gia_id, khach_hang_id)
);

-- Thêm comment
COMMENT ON TABLE phieu_giam_gia_ca_nhan IS 'Bảng liên kết phiếu giảm giá với khách hàng cá nhân';
COMMENT ON COLUMN phieu_giam_gia_ca_nhan.id IS 'ID tự tăng';
COMMENT ON COLUMN phieu_giam_gia_ca_nhan.khach_hang_id IS 'ID khách hàng';
COMMENT ON COLUMN phieu_giam_gia_ca_nhan.phieu_giam_gia_id IS 'ID phiếu giảm giá';

-- Thêm dữ liệu mẫu (nếu cần)
-- INSERT INTO phieu_giam_gia_ca_nhan (khach_hang_id, phieu_giam_gia_id) VALUES 
-- (1, 1),
-- (2, 1),
-- (1, 2);
