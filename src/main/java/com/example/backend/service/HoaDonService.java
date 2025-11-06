package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.dto.HoaDonChiTietDTO;
import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonChiTiet;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.ChiTietSanPham;
import com.example.backend.entity.HinhThucThanhToan;
import com.example.backend.entity.PhuongThucThanhToan;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.ChiTietSanPhamRepository;
import com.example.backend.repository.HinhThucThanhToanRepository;
import com.example.backend.repository.PhuongThucThanhToanRepository;
import com.example.backend.service.DiaChiKhachHangService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final DiaChiKhachHangService diaChiKhachHangService;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public HoaDonService(HoaDonRepository hoaDonRepository, 
                         KhachHangRepository khachHangRepository,
                         NhanVienRepository nhanVienRepository,
                         ChiTietSanPhamRepository chiTietSanPhamRepository,
                         DiaChiKhachHangService diaChiKhachHangService,
                         HinhThucThanhToanRepository hinhThucThanhToanRepository,
                         PhuongThucThanhToanRepository phuongThucThanhToanRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.diaChiKhachHangService = diaChiKhachHangService;
        this.hinhThucThanhToanRepository = hinhThucThanhToanRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
    }

    public HoaDonDTO toDTO(HoaDon h) {
        HoaDonDTO.HoaDonDTOBuilder builder = HoaDonDTO.builder()
                .id(h.getId())
                .maHoaDon(h.getMaHoaDon())
                .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                .ngayTao(h.getNgayTao())
                .ngayThanhToan(h.getNgayThanhToan())
                .tongTien(h.getTongTien())
                .tienGiamGia(h.getTienGiamGia())
                .giamGiaPhanTram(h.getGiamGiaPhanTram())
                .thanhTien(h.getThanhTien())
                .ghiChu(h.getGhiChu())
                .trangThai(h.getTrangThai())
                .soLuongSanPham(h.getSoLuongSanPham());
        
        // Map thông tin khách hàng
        if (h.getKhachHang() != null) {
            builder.tenKhachHang(h.getKhachHang().getTenKhachHang())
                   .emailKhachHang(h.getKhachHang().getEmail())
                   .soDienThoaiKhachHang(h.getKhachHang().getSoDienThoai())
                   .diaChiKhachHang(h.getKhachHang().getDiaChi());
            
            // Lấy địa chỉ mặc định của khách hàng từ bảng dia_chi_khach_hang
            try {
                Optional<DiaChiKhachHangDTO> diaChiMacDinh = diaChiKhachHangService.getDiaChiMacDinhByKhachHangId(h.getKhachHang().getId());
                if (diaChiMacDinh.isPresent()) {
                    DiaChiKhachHangDTO diaChi = diaChiMacDinh.get();
                    builder.diaChiChiTiet(diaChi.getDiaChiChiTiet())
                           .tinhThanh(diaChi.getTinhThanh())
                           .quanHuyen(diaChi.getQuanHuyen())
                           .phuongXa(diaChi.getPhuongXa());
                    
                    // Tạo địa chỉ đầy đủ để hiển thị
                    String diaChiFull = String.format("%s, %s, %s, %s", 
                        diaChi.getDiaChiChiTiet() != null ? diaChi.getDiaChiChiTiet() : "",
                        diaChi.getPhuongXa() != null ? diaChi.getPhuongXa() : "",
                        diaChi.getQuanHuyen() != null ? diaChi.getQuanHuyen() : "",
                        diaChi.getTinhThanh() != null ? diaChi.getTinhThanh() : "");
                    builder.diaChiKhachHang(diaChiFull.trim().replaceAll("^,\\s*|,\\s*$", ""));
                }
            } catch (Exception e) {
                // Nếu không lấy được địa chỉ, giữ nguyên địa chỉ từ khách hàng
            }
        }
        
        // Map thông tin nhân viên
        if (h.getNhanVien() != null) {
            builder.tenNhanVien(h.getNhanVien().getHoTen());
        }
        
        // Map phương thức thanh toán từ list PhuongThucThanhToan
        // Load riêng để tránh MultipleBagFetchException
        String phuongThucValue = null;
        try {
            List<PhuongThucThanhToan> ptttList = phuongThucThanhToanRepository.findByHoaDonId(h.getId());
            if (!ptttList.isEmpty()) {
                var pttt = ptttList.get(0);
                if (pttt.getHinhThucThanhToan() != null) {
                    String tenHinhThuc = pttt.getHinhThucThanhToan().getTenHinhThuc();
                    // Map lại về format frontend: "Tiền mặt" -> "cash", "Chuyển khoản" -> "transfer"
                    if ("Tiền mặt".equals(tenHinhThuc) || "Tiền Mặt".equals(tenHinhThuc)) {
                        phuongThucValue = "cash";
                    } else if ("Chuyển khoản".equals(tenHinhThuc) || "Chuyển Khoản".equals(tenHinhThuc)) {
                        phuongThucValue = "transfer";
                    } else {
                        phuongThucValue = tenHinhThuc;
                    }
                }
            }
        } catch (Exception e) {
            // Nếu không load được, bỏ qua
        }
        // Set giá trị hoặc default là "cash"
        builder.phuongThucThanhToan(phuongThucValue != null ? phuongThucValue : "cash");
        
        // Map danh sách chi tiết sản phẩm
        if (h.getDanhSachChiTiet() != null && !h.getDanhSachChiTiet().isEmpty()) {
            List<HoaDonChiTietDTO> chiTietList = h.getDanhSachChiTiet().stream()
                    .map(this::toChiTietDTO)
                    .collect(Collectors.toList());
            builder.danhSachChiTiet(chiTietList);
        }
        
        return builder.build();
    }
    
    private HoaDonChiTietDTO toChiTietDTO(HoaDonChiTiet ct) {
        HoaDonChiTietDTO.HoaDonChiTietDTOBuilder builder = HoaDonChiTietDTO.builder()
                .id(ct.getId())
                .chiTietSanPhamId(ct.getChiTietSanPham() != null ? ct.getChiTietSanPham().getId() : null)
                .soLuong(ct.getSoLuong())
                .donGia(ct.getDonGia())
                .giamGia(ct.getGiamGia())
                .thanhTien(ct.getThanhTien());
        
        // Map thông tin sản phẩm từ chi_tiet_san_pham
        if (ct.getChiTietSanPham() != null) {
            var chiTietSP = ct.getChiTietSanPham();
            
            // Lấy thông tin từ san_pham
            if (chiTietSP.getSanPham() != null) {
                var sanPham = chiTietSP.getSanPham();
                builder.tenSanPham(sanPham.getTenSanPham())
                       .maSanPham(sanPham.getMaSanPham())
                       .anhSanPham(sanPham.getAnhSanPham());
                
                // Lấy nhà sản xuất
                if (sanPham.getNhaSanXuat() != null) {
                    builder.nhaSanXuat(sanPham.getNhaSanXuat().getTenNhaSanXuat());
                }
            }
            
            // Lấy màu sắc
            if (chiTietSP.getMauSac() != null) {
                builder.mauSac(chiTietSP.getMauSac().getTenMau());
            }
            
            // Lấy kích thước
            if (chiTietSP.getKichThuoc() != null) {
                builder.kichThuoc(chiTietSP.getKichThuoc().getTenKichThuoc());
            }
        }
        
        return builder.build();
    }

    private void updateEntityFromDTO(HoaDon h, HoaDonDTO d) {
        if (d.getMaHoaDon() != null) h.setMaHoaDon(d.getMaHoaDon());
        if (d.getNgayThanhToan() != null) h.setNgayThanhToan(d.getNgayThanhToan());
        if (d.getTongTien() != null) h.setTongTien(d.getTongTien());
        if (d.getTienGiamGia() != null) h.setTienGiamGia(d.getTienGiamGia());
        if (d.getGiamGiaPhanTram() != null) h.setGiamGiaPhanTram(d.getGiamGiaPhanTram());
        if (d.getThanhTien() != null) h.setThanhTien(d.getThanhTien());
        if (d.getGhiChu() != null) h.setGhiChu(d.getGhiChu());
        if (d.getTrangThai() != null) h.setTrangThai(d.getTrangThai());
        if (d.getSoLuongSanPham() != null) h.setSoLuongSanPham(d.getSoLuongSanPham());
        
        // Map khách hàng từ ID
        if (d.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(d.getKhachHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + d.getKhachHangId()));
            h.setKhachHang(khachHang);
        }
        
        // Map nhân viên từ ID
        if (d.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(d.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + d.getNhanVienId()));
            h.setNhanVien(nhanVien);
        }
    }

    @Transactional
    public HoaDonDTO createHoaDon(HoaDonDTO dto) {
        HoaDon h = new HoaDon();
        h.setMaHoaDon(dto.getMaHoaDon());
        h.setNgayTao(LocalDateTime.now());
        updateEntityFromDTO(h, dto);
        
        // Xử lý danh sách chi tiết sản phẩm nếu có
        if (dto.getDanhSachChiTiet() != null && !dto.getDanhSachChiTiet().isEmpty()) {
            List<HoaDonChiTiet> chiTietList = new ArrayList<>();
            for (HoaDonChiTietDTO chiTietDTO : dto.getDanhSachChiTiet()) {
                if (chiTietDTO.getChiTietSanPhamId() == null) {
                    continue; // Bỏ qua nếu không có chiTietSanPhamId
                }
                
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietDTO.getChiTietSanPhamId()));
                
                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHoaDon(h);
                hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
                hoaDonChiTiet.setSoLuong(chiTietDTO.getSoLuong() != null ? chiTietDTO.getSoLuong() : 0);
                hoaDonChiTiet.setDonGia(chiTietDTO.getDonGia() != null ? chiTietDTO.getDonGia() : java.math.BigDecimal.ZERO);
                hoaDonChiTiet.setGiamGia(chiTietDTO.getGiamGia() != null ? chiTietDTO.getGiamGia() : java.math.BigDecimal.ZERO);
                hoaDonChiTiet.setThanhTien(chiTietDTO.getThanhTien() != null ? chiTietDTO.getThanhTien() : java.math.BigDecimal.ZERO);
                
                chiTietList.add(hoaDonChiTiet);
            }
            h.setDanhSachChiTiet(chiTietList);
        }
        
        // Lưu hóa đơn trước để có ID
        HoaDon saved = hoaDonRepository.save(h);
        
        // Xử lý phương thức thanh toán nếu có
        if (dto.getPhuongThucThanhToan() != null && !dto.getPhuongThucThanhToan().trim().isEmpty()) {
            String phuongThucTen = dto.getPhuongThucThanhToan().trim();
            // Map từ frontend: "cash" -> "Tiền mặt", "transfer" -> "Chuyển khoản"
            String tenHinhThuc;
            if ("cash".equalsIgnoreCase(phuongThucTen)) {
                tenHinhThuc = "Tiền mặt";
            } else if ("transfer".equalsIgnoreCase(phuongThucTen)) {
                tenHinhThuc = "Chuyển khoản";
            } else {
                tenHinhThuc = phuongThucTen; // Giữ nguyên nếu là tên khác
            }
            
            // Tìm hoặc tạo HinhThucThanhToan
            HinhThucThanhToan hinhThuc = hinhThucThanhToanRepository.findByTenHinhThuc(tenHinhThuc)
                    .orElseGet(() -> {
                        HinhThucThanhToan newHinhThuc = new HinhThucThanhToan();
                        newHinhThuc.setTenHinhThuc(tenHinhThuc);
                        newHinhThuc.setTrangThai(true);
                        return hinhThucThanhToanRepository.save(newHinhThuc);
                    });
            
            // Tạo PhuongThucThanhToan
            PhuongThucThanhToan pttt = new PhuongThucThanhToan();
            pttt.setHoaDon(saved);
            pttt.setHinhThucThanhToan(hinhThuc);
            pttt.setSoTienThanhToan(saved.getThanhTien() != null ? saved.getThanhTien() : java.math.BigDecimal.ZERO);
            pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN);
            phuongThucThanhToanRepository.save(pttt);
        }
        
        // Reload hóa đơn bằng cách gọi getHoaDonById để đảm bảo có dữ liệu đầy đủ
        // Lưu ý: Không fetch phuongThucThanhToan trong query để tránh MultipleBagFetchException
        Optional<HoaDon> reloaded = getHoaDonById(saved.getId());
        if (reloaded.isPresent()) {
            return toDTO(reloaded.get());
        }
        return toDTO(saved);
    }

    @Transactional
    public HoaDonDTO updateHoaDon(Long id, HoaDonDTO dto) {

        HoaDon h = getHoaDonById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        
        updateEntityFromDTO(h, dto);
        
        // Xử lý danh sách chi tiết sản phẩm nếu có
        // QUAN TRỌNG: Với orphanRemoval = true, KHÔNG được set collection mới hoặc clear() mà không add lại ngay
        // Giải pháp: Clear và add lại trong cùng một block, đảm bảo collection luôn có reference
        if (dto.getDanhSachChiTiet() != null) {
            // Đảm bảo collection được khởi tạo trước
            if (h.getDanhSachChiTiet() == null) {
                h.setDanhSachChiTiet(new ArrayList<>());
            }
            
            // Xóa các chi tiết cũ bằng cách clear() collection
            // Với orphanRemoval = true, clear() sẽ tự động xóa các item khỏi database
            h.getDanhSachChiTiet().clear();

            
            // Ngay lập tức add các chi tiết mới vào collection (không được để collection rỗng quá lâu)
            if (!dto.getDanhSachChiTiet().isEmpty()) {
                for (HoaDonChiTietDTO chiTietDTO : dto.getDanhSachChiTiet()) {
                    if (chiTietDTO.getChiTietSanPhamId() == null) {
                        continue; // Bỏ qua nếu không có chiTietSanPhamId
                    }
                    
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietDTO.getChiTietSanPhamId()));
                    
                    HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                    hoaDonChiTiet.setHoaDon(h);
                    hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
                    hoaDonChiTiet.setSoLuong(chiTietDTO.getSoLuong() != null ? chiTietDTO.getSoLuong() : 0);
                    hoaDonChiTiet.setDonGia(chiTietDTO.getDonGia() != null ? chiTietDTO.getDonGia() : java.math.BigDecimal.ZERO);
                    hoaDonChiTiet.setGiamGia(chiTietDTO.getGiamGia() != null ? chiTietDTO.getGiamGia() : java.math.BigDecimal.ZERO);
                    hoaDonChiTiet.setThanhTien(chiTietDTO.getThanhTien() != null ? chiTietDTO.getThanhTien() : java.math.BigDecimal.ZERO);
                    
                    // Add ngay vào collection sau khi clear (không được delay)
                    h.getDanhSachChiTiet().add(hoaDonChiTiet);

                }
            }
            // Nếu danhSachChiTiet là empty array, collection đã được clear và giữ nguyên empty
        }
        // Nếu dto.getDanhSachChiTiet() == null, giữ nguyên collection hiện tại (không thay đổi)
        
        // Lưu hóa đơn
        HoaDon saved = hoaDonRepository.save(h);
        
        // Xử lý phương thức thanh toán nếu có
        // Xóa các phương thức thanh toán cũ trước
        List<PhuongThucThanhToan> existingPttt = phuongThucThanhToanRepository.findByHoaDonId(saved.getId());
        if (!existingPttt.isEmpty()) {
            phuongThucThanhToanRepository.deleteAll(existingPttt);
        }
        
        // Tạo phương thức thanh toán mới nếu có
        if (dto.getPhuongThucThanhToan() != null && !dto.getPhuongThucThanhToan().trim().isEmpty()) {
            String phuongThucTen = dto.getPhuongThucThanhToan().trim();
            // Map từ frontend: "cash" -> "Tiền mặt", "transfer" -> "Chuyển khoản"
            String tenHinhThuc;
            if ("cash".equalsIgnoreCase(phuongThucTen)) {
                tenHinhThuc = "Tiền mặt";
            } else if ("transfer".equalsIgnoreCase(phuongThucTen)) {
                tenHinhThuc = "Chuyển khoản";
            } else {
                tenHinhThuc = phuongThucTen; // Giữ nguyên nếu là tên khác
            }
            
            // Tìm hoặc tạo HinhThucThanhToan
            HinhThucThanhToan hinhThuc = hinhThucThanhToanRepository.findByTenHinhThuc(tenHinhThuc)
                    .orElseGet(() -> {
                        HinhThucThanhToan newHinhThuc = new HinhThucThanhToan();
                        newHinhThuc.setTenHinhThuc(tenHinhThuc);
                        newHinhThuc.setTrangThai(true);
                        return hinhThucThanhToanRepository.save(newHinhThuc);
                    });
            
            // Tạo PhuongThucThanhToan
            PhuongThucThanhToan pttt = new PhuongThucThanhToan();
            pttt.setHoaDon(saved);
            pttt.setHinhThucThanhToan(hinhThuc);
            pttt.setSoTienThanhToan(saved.getThanhTien() != null ? saved.getThanhTien() : java.math.BigDecimal.ZERO);
            pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN);
            phuongThucThanhToanRepository.save(pttt);
        }
        
        // Reload hóa đơn để đảm bảo có dữ liệu đầy đủ
        Optional<HoaDon> reloaded = getHoaDonById(saved.getId());
        if (reloaded.isPresent()) {
            return toDTO(reloaded.get());
        }
        return toDTO(saved);
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        hoaDonRepository.delete(h);
    }

    /**
     * Cập nhật trạng thái hóa đơn
     */
    @Transactional
    public HoaDonDTO updateTrangThaiHoaDon(Long id, String trangThai) {
        // Load hóa đơn
        HoaDon h = getHoaDonById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn với ID: " + id));
        
        // Validate và set trạng thái
        try {
            HoaDon.TrangThaiHoaDon newTrangThai = HoaDon.TrangThaiHoaDon.valueOf(trangThai);
            h.setTrangThai(newTrangThai);
            
            // Lưu vào database
            HoaDon saved = hoaDonRepository.save(h);
            
            // Reload để đảm bảo có dữ liệu đầy đủ
            Optional<HoaDon> reloaded = getHoaDonById(saved.getId());
            if (reloaded.isPresent()) {
                return toDTO(reloaded.get());
            }
            return toDTO(saved);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + trangThai);
        }
    }

    public Page<HoaDon> getAllHoaDon(String keyword, String phuongThucThanhToan, String trangThai, Pageable pageable) {
        // Map từ frontend format sang backend format
        String tenHinhThuc = null;
        if (phuongThucThanhToan != null && !phuongThucThanhToan.trim().isEmpty()) {
            if ("cash".equalsIgnoreCase(phuongThucThanhToan)) {
                tenHinhThuc = "Tiền mặt";
            } else if ("transfer".equalsIgnoreCase(phuongThucThanhToan)) {
                tenHinhThuc = "Chuyển khoản";
            } else {
                tenHinhThuc = phuongThucThanhToan;
            }
        }
        
        // Map trangThai từ String (frontend) sang enum (backend)
        // Frontend gửi: CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO_HANG, DA_GIAO_HANG, HUY
        // Backend enum: CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO_HANG, DA_GIAO_HANG, DA_HUY
        HoaDon.TrangThaiHoaDon trangThaiEnum = null;
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            try {
                String trangThaiUpper = trangThai.toUpperCase();
                // Map HUY từ frontend sang DA_HUY trong backend
                if ("HUY".equals(trangThaiUpper)) {
                    trangThaiUpper = "DA_HUY";
                }
                trangThaiEnum = HoaDon.TrangThaiHoaDon.valueOf(trangThaiUpper);
                System.out.println("✅ Mapped trangThai: " + trangThai + " -> " + trangThaiEnum.name());
            } catch (IllegalArgumentException e) {
                System.err.println("⚠️ Invalid trangThai value: " + trangThai);
                System.err.println("💡 Valid values: CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO_HANG, DA_GIAO_HANG, HUY");
                trangThaiEnum = null;
            }
        }
        
        // Đếm tổng số bản ghi trước
        StringBuilder countQueryStr = new StringBuilder(
            "SELECT COUNT(DISTINCT h) FROM HoaDon h " +
            "LEFT JOIN h.phuongThucThanhToan pttt " +
            "LEFT JOIN pttt.hinhThucThanhToan htt " +
            "WHERE (:keyword IS NULL OR h.maHoaDon LIKE :keyword)"
        );
        
        if (tenHinhThuc != null) {
            countQueryStr.append(" AND (htt.tenHinhThuc = :tenHinhThuc OR htt.tenHinhThuc IS NULL)");
        }
        
        if (trangThaiEnum != null) {
            countQueryStr.append(" AND h.trangThai = :trangThai");
        }
        
        jakarta.persistence.TypedQuery<Long> countQuery = entityManager.createQuery(
            countQueryStr.toString(),
            Long.class
        );
        
        if (keyword != null && !keyword.isEmpty()) {
            countQuery.setParameter("keyword", "%" + keyword + "%");
        } else {
            countQuery.setParameter("keyword", null);
        }
        
        if (tenHinhThuc != null) {
            countQuery.setParameter("tenHinhThuc", tenHinhThuc);
        }
        
        if (trangThaiEnum != null) {
            countQuery.setParameter("trangThai", trangThaiEnum);
        }
        
        long totalElements = countQuery.getSingleResult();
        
        // Query với join fetch để load các relationships
        // Lưu ý: Không thể fetch nhiều collections cùng lúc, nên chỉ fetch danhSachChiTiet
        StringBuilder queryStr = new StringBuilder(
            "SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.nhanVien " +
            "LEFT JOIN FETCH h.danhSachChiTiet c " +
            "LEFT JOIN FETCH c.chiTietSanPham ct " +
            "LEFT JOIN FETCH ct.sanPham s " +
            "LEFT JOIN FETCH s.nhaSanXuat " +
            "LEFT JOIN FETCH ct.mauSac " +
            "LEFT JOIN FETCH ct.kichThuoc " +
            "LEFT JOIN h.phuongThucThanhToan pttt " +
            "LEFT JOIN pttt.hinhThucThanhToan htt " +
            "WHERE (:keyword IS NULL OR h.maHoaDon LIKE :keyword)"
        );
        
        if (tenHinhThuc != null) {
            queryStr.append(" AND (htt.tenHinhThuc = :tenHinhThuc OR htt.tenHinhThuc IS NULL)");
        }
        
        if (trangThaiEnum != null) {
            queryStr.append(" AND h.trangThai = :trangThai");
        }
        
        // Luôn có ORDER BY để đảm bảo thứ tự: mặc định ORDER BY id ASC (hóa đơn cũ nhất lên đầu, mới nhất xuống cuối)
        // Chỉ thay đổi ORDER BY nếu user click vào cột để sort
        Sort sort = pageable.getSort();
        if (sort != null && sort.isSorted()) {
            // User đã click sort - sử dụng sort của user
            Sort.Order order = sort.iterator().next();
            String sortField = order.getProperty();
            String sortDir = order.getDirection().name();
            queryStr.append(" ORDER BY h.").append(sortField).append(" ").append(sortDir);
        } else {
            // Mặc định: ORDER BY id ASC - hóa đơn mới nhất (ID lớn nhất) ở cuối
            queryStr.append(" ORDER BY h.id ASC");
        }
        
        jakarta.persistence.TypedQuery<HoaDon> query = entityManager.createQuery(
            queryStr.toString(),
            HoaDon.class
        );
        
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        } else {
            query.setParameter("keyword", null);
        }
        
        if (tenHinhThuc != null) {
            query.setParameter("tenHinhThuc", tenHinhThuc);
        }
        
        if (trangThaiEnum != null) {
            query.setParameter("trangThai", trangThaiEnum);
        }
        
        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<HoaDon> results = query.getResultList();
        
        // Create a Page manually
        return new org.springframework.data.domain.PageImpl<>(results, pageable, totalElements);
    }

    public Optional<HoaDon> getHoaDonById(Long id) {
        jakarta.persistence.TypedQuery<HoaDon> query = entityManager.createQuery(
            "SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.nhanVien " +
            "LEFT JOIN FETCH h.danhSachChiTiet c " +
            "LEFT JOIN FETCH c.chiTietSanPham ct " +
            "LEFT JOIN FETCH ct.sanPham s " +
            "LEFT JOIN FETCH s.nhaSanXuat " +
            "LEFT JOIN FETCH ct.mauSac " +
            "LEFT JOIN FETCH ct.kichThuoc " +
            "WHERE h.id = :id",
            HoaDon.class
        );
        query.setParameter("id", id);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    public Page<HoaDon> getHoaDonByKhachHangId(Long khachHangId, Pageable pageable) {
        // Đếm tổng số bản ghi
        jakarta.persistence.TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(DISTINCT h) FROM HoaDon h " +
            "WHERE h.khachHang.id = :khachHangId",
            Long.class
        );
        countQuery.setParameter("khachHangId", khachHangId);
        long totalElements = countQuery.getSingleResult();
        
        // Query với join fetch để load các relationships
        jakarta.persistence.TypedQuery<HoaDon> query = entityManager.createQuery(
            "SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.nhanVien " +
            "LEFT JOIN FETCH h.danhSachChiTiet c " +
            "LEFT JOIN FETCH c.chiTietSanPham ct " +
            "LEFT JOIN FETCH ct.sanPham s " +
            "LEFT JOIN FETCH s.nhaSanXuat " +
            "LEFT JOIN FETCH ct.mauSac " +
            "LEFT JOIN FETCH ct.kichThuoc " +
            "WHERE h.khachHang.id = :khachHangId " +
            "ORDER BY h.ngayTao DESC",
            HoaDon.class
        );
        query.setParameter("khachHangId", khachHangId);
        
        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<HoaDon> results = query.getResultList();
        
        // Create a Page manually
        return new org.springframework.data.domain.PageImpl<>(results, pageable, totalElements);
    }
}
