-- Migration: Add phi_giao_hang column to hoa_don table
-- Date: 2025-01-XX
-- Description: Thêm cột phí giao hàng vào bảng hóa đơn

-- Kiểm tra xem cột đã tồn tại chưa, nếu chưa thì thêm
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'hoa_don' 
        AND column_name = 'phi_giao_hang'
    ) THEN
        ALTER TABLE hoa_don 
        ADD COLUMN phi_giao_hang DECIMAL(15, 2) DEFAULT 0;
        
        -- Cập nhật giá trị mặc định cho các hóa đơn hiện có (nếu cần)
        UPDATE hoa_don 
        SET phi_giao_hang = 0 
        WHERE phi_giao_hang IS NULL;
        
        RAISE NOTICE 'Column phi_giao_hang added to hoa_don table';
    ELSE
        RAISE NOTICE 'Column phi_giao_hang already exists in hoa_don table';
    END IF;
END $$;



