-- Tạo bảng phieu_giam_gia
CREATE TABLE IF NOT EXISTS phieu_giam_gia (
    id BIGSERIAL PRIMARY KEY,
    ma_phieu VARCHAR(50) NOT NULL UNIQUE,
    ten_phieu_giam_gia VARCHAR(255) NOT NULL,
    loai_phieu_giam_gia BOOLEAN NOT NULL DEFAULT false, -- false = phần trăm, true = tiền mặt
    gia_tri_giam DECIMAL(38,2) NOT NULL,
    gia_tri_toi_thieu DECIMAL(38,2) NOT NULL,
    so_tien_toi_da DECIMAL(38,2) NOT NULL,
    hoa_don_toi_thieu DECIMAL(38,2) NOT NULL,
    so_luong_dung INTEGER NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    trang_thai BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng phieu_giam_gia_ca_nhan
CREATE TABLE IF NOT EXISTS phieu_giam_gia_ca_nhan (
    id BIGSERIAL PRIMARY KEY,
    phieu_giam_gia_id BIGINT NOT NULL,
    khach_hang_id BIGINT NOT NULL,
    so_lan_da_dung INTEGER DEFAULT 0,
    trang_thai BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id) ON DELETE CASCADE,
    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE,
    UNIQUE(phieu_giam_gia_id, khach_hang_id)
);

-- Thêm dữ liệu mẫu
INSERT INTO phieu_giam_gia (
    ma_phieu, 
    ten_phieu_giam_gia, 
    loai_phieu_giam_gia, 
    gia_tri_giam, 
    gia_tri_toi_thieu, 
    so_tien_toi_da, 
    hoa_don_toi_thieu, 
    so_luong_dung, 
    ngay_bat_dau, 
    ngay_ket_thuc, 
    trang_thai
) VALUES 
(
    'PGG001', 
    'Giảm giá 10% cho đơn hàng từ 500k', 
    false, 
    10.00, 
    500000.00, 
    100000.00, 
    500000.00, 
    100, 
    '2024-01-01', 
    '2024-12-31', 
    true
),
(
    'PGG002', 
    'Giảm giá 50k cho đơn hàng từ 1 triệu', 
    true, 
    50000.00, 
    1000000.00, 
    50000.00, 
    1000000.00, 
    50, 
    '2024-01-01', 
    '2024-12-31', 
    true
),
(
    'PGG003', 
    'Giảm giá 20% cho khách hàng VIP', 
    false, 
    20.00, 
    200000.00, 
    200000.00, 
    200000.00, 
    20, 
    '2024-01-01', 
    '2024-12-31', 
    true
);

-- Tạo index để tối ưu hiệu suất
CREATE INDEX IF NOT EXISTS idx_phieu_giam_gia_ma_phieu ON phieu_giam_gia(ma_phieu);
CREATE INDEX IF NOT EXISTS idx_phieu_giam_gia_trang_thai ON phieu_giam_gia(trang_thai);
CREATE INDEX IF NOT EXISTS idx_phieu_giam_gia_ngay_bat_dau ON phieu_giam_gia(ngay_bat_dau);
CREATE INDEX IF NOT EXISTS idx_phieu_giam_gia_ngay_ket_thuc ON phieu_giam_gia(ngay_ket_thuc);
CREATE INDEX IF NOT EXISTS idx_phieu_giam_gia_ca_nhan_phieu_id ON phieu_giam_gia_ca_nhan(phieu_giam_gia_id);
CREATE INDEX IF NOT EXISTS idx_phieu_giam_gia_ca_nhan_khach_hang_id ON phieu_giam_gia_ca_nhan(khach_hang_id);
