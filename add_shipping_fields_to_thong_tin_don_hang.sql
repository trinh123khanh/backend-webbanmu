-- Script để thêm các trường vận chuyển vào bảng thong_tin_don_hang
-- Các trường: khoi_luong, chieu_dai, chieu_rong, chieu_cao

-- Kiểm tra và thêm cột khoi_luong nếu chưa có
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'thong_tin_don_hang' 
        AND column_name = 'khoi_luong'
    ) THEN
        ALTER TABLE thong_tin_don_hang 
        ADD COLUMN khoi_luong DECIMAL(10, 2);
        RAISE NOTICE 'Đã thêm cột khoi_luong vào bảng thong_tin_don_hang';
    ELSE
        RAISE NOTICE 'Cột khoi_luong đã tồn tại trong bảng thong_tin_don_hang';
    END IF;
END $$;

-- Kiểm tra và thêm cột chieu_dai nếu chưa có
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'thong_tin_don_hang' 
        AND column_name = 'chieu_dai'
    ) THEN
        ALTER TABLE thong_tin_don_hang 
        ADD COLUMN chieu_dai DECIMAL(10, 2);
        RAISE NOTICE 'Đã thêm cột chieu_dai vào bảng thong_tin_don_hang';
    ELSE
        RAISE NOTICE 'Cột chieu_dai đã tồn tại trong bảng thong_tin_don_hang';
    END IF;
END $$;

-- Kiểm tra và thêm cột chieu_rong nếu chưa có
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'thong_tin_don_hang' 
        AND column_name = 'chieu_rong'
    ) THEN
        ALTER TABLE thong_tin_don_hang 
        ADD COLUMN chieu_rong DECIMAL(10, 2);
        RAISE NOTICE 'Đã thêm cột chieu_rong vào bảng thong_tin_don_hang';
    ELSE
        RAISE NOTICE 'Cột chieu_rong đã tồn tại trong bảng thong_tin_don_hang';
    END IF;
END $$;

-- Kiểm tra và thêm cột chieu_cao nếu chưa có
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'thong_tin_don_hang' 
        AND column_name = 'chieu_cao'
    ) THEN
        ALTER TABLE thong_tin_don_hang 
        ADD COLUMN chieu_cao DECIMAL(10, 2);
        RAISE NOTICE 'Đã thêm cột chieu_cao vào bảng thong_tin_don_hang';
    ELSE
        RAISE NOTICE 'Cột chieu_cao đã tồn tại trong bảng thong_tin_don_hang';
    END IF;
END $$;

-- Thêm comment cho các cột mới
COMMENT ON COLUMN thong_tin_don_hang.khoi_luong IS 'Khối lượng đơn hàng (kg)';
COMMENT ON COLUMN thong_tin_don_hang.chieu_dai IS 'Chiều dài đơn hàng (cm)';
COMMENT ON COLUMN thong_tin_don_hang.chieu_rong IS 'Chiều rộng đơn hàng (cm)';
COMMENT ON COLUMN thong_tin_don_hang.chieu_cao IS 'Chiều cao đơn hàng (cm)';

