-- Kiểm tra bảng và cột lưu số lượng tồn kho
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'chi_tiet_san_pham' 
  AND column_name = 'so_luong_ton';

-- Xem số lượng tồn kho hiện tại của tất cả sản phẩm
SELECT 
    csp.id AS chi_tiet_san_pham_id,
    sp.ten_san_pham,
    ms.ten_mau AS mau_sac,
    kt.ten_kich_thuoc AS kich_thuoc,
    csp.so_luong_ton AS so_luong_hien_tai,
    csp.gia_ban
FROM chi_tiet_san_pham csp
JOIN san_pham sp ON csp.san_pham_id = sp.id
LEFT JOIN mau_sac ms ON csp.mau_sac_id = ms.id
LEFT JOIN kich_thuoc kt ON csp.kich_thuoc_id = kt.id
ORDER BY sp.ten_san_pham, csp.id;

-- Xem các hoá đơn đã trừ số lượng (status = CHO_XAC_NHAN hoặc DA_XAC_NHAN)
SELECT 
    hd.id AS hoa_don_id,
    hd.ma_hoa_don,
    hd.trang_thai,
    hd.ngay_tao,
    hdct.so_luong AS so_luong_mua,
    csp.id AS chi_tiet_san_pham_id,
    csp.so_luong_ton AS so_luong_hien_tai,
    sp.ten_san_pham,
    CASE 
        WHEN hd.nhan_vien_id IS NULL THEN 'Online'
        ELSE 'Tại quầy'
    END AS loai_don_hang
FROM hoa_don hd
JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.hoa_don_id
JOIN chi_tiet_san_pham csp ON hdct.chi_tiet_san_pham_id = csp.id
JOIN san_pham sp ON csp.san_pham_id = sp.id
WHERE hd.trang_thai IN ('CHO_XAC_NHAN', 'DA_XAC_NHAN')
ORDER BY hd.ngay_tao DESC
LIMIT 20;
