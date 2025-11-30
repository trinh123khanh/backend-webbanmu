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
import com.example.backend.entity.ThongTinDonHang;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.ChiTietSanPhamRepository;
import com.example.backend.repository.HinhThucThanhToanRepository;
import com.example.backend.repository.PhuongThucThanhToanRepository;
import com.example.backend.repository.HoaDonChiTietRepository;
import com.example.backend.repository.ThongTinDonHangRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThongTinDonHangRepository thongTinDonHangRepository;
    private final HoaDonActivityService hoaDonActivityService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    
    @PersistenceContext
    private EntityManager entityManager;

    public HoaDonService(HoaDonRepository hoaDonRepository, 
                         KhachHangRepository khachHangRepository,
                         NhanVienRepository nhanVienRepository,
                         ChiTietSanPhamRepository chiTietSanPhamRepository,
                         DiaChiKhachHangService diaChiKhachHangService,
                         HinhThucThanhToanRepository hinhThucThanhToanRepository,
                         PhuongThucThanhToanRepository phuongThucThanhToanRepository,
                         HoaDonChiTietRepository hoaDonChiTietRepository,
                         ThongTinDonHangRepository thongTinDonHangRepository,
                         HoaDonActivityService hoaDonActivityService,
                         EmailService emailService,
                         ObjectMapper objectMapper) {
        this.hoaDonRepository = hoaDonRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.diaChiKhachHangService = diaChiKhachHangService;
        this.hinhThucThanhToanRepository = hinhThucThanhToanRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thongTinDonHangRepository = thongTinDonHangRepository;
        this.hoaDonActivityService = hoaDonActivityService;
        this.emailService = emailService;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Extract diaChiChiTiet từ diaChiGiaoHang (format: "dcw, Xã Liên Châu, Huyện Yên Lạc, Tỉnh Vĩnh Phúc")
     */
    private String extractDiaChiChiTiet(String diaChiGiaoHang) {
        if (diaChiGiaoHang == null || diaChiGiaoHang.trim().isEmpty()) {
            return "";
        }
        // Lấy phần đầu tiên trước dấu phẩy đầu tiên
        String[] parts = diaChiGiaoHang.split(",");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return diaChiGiaoHang.trim();
    }
    
    public HoaDonDTO toDTO(HoaDon h) {
        // ✅ QUAN TRỌNG: Tính toán thanhTien đúng logic = tongTien - tienGiamGia + phiGiaoHang
        // ✅ QUAN TRỌNG: Ưu tiên lấy phiGiaoHang từ entity HoaDon (bảng hoa_don.phi_giao_hang)
        java.math.BigDecimal phiGiaoHang = h.getPhiGiaoHang() != null ? h.getPhiGiaoHang() : java.math.BigDecimal.ZERO;
        System.out.println("💰 [toDTO] phiGiaoHang from HoaDon entity (hoa_don.phi_giao_hang): " + phiGiaoHang + " (Invoice ID: " + h.getId() + ")");
        
        java.math.BigDecimal tongTien = h.getTongTien() != null ? h.getTongTien() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal tienGiamGia = h.getTienGiamGia() != null ? h.getTienGiamGia() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal calculatedThanhTien = tongTien.subtract(tienGiamGia).add(phiGiaoHang).max(java.math.BigDecimal.ZERO);
        
        HoaDonDTO.HoaDonDTOBuilder builder = HoaDonDTO.builder()
                .id(h.getId())
                .maHoaDon(h.getMaHoaDon())
                .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                .ngayTao(h.getNgayTao())
                .ngayThanhToan(h.getNgayThanhToan())
                .tongTien(tongTien)
                .tienGiamGia(tienGiamGia)
                .giamGiaPhanTram(h.getGiamGiaPhanTram())
                .thanhTien(calculatedThanhTien) // Sử dụng calculatedThanhTien thay vì h.getThanhTien()
                .ghiChu(h.getGhiChu())
                .trangThai(convertTrangThaiEnumToString(h.getTrangThai()))
                .soLuongSanPham(h.getSoLuongSanPham())
                .phiGiaoHang(phiGiaoHang); // ✅ Set phiGiaoHang từ entity trước
        
        // Map thông tin khách hàng
        if (h.getKhachHang() != null) {
            builder.tenKhachHang(h.getKhachHang().getTenKhachHang())
                   .emailKhachHang(h.getKhachHang().getEmail())
                   .soDienThoaiKhachHang(h.getKhachHang().getSoDienThoai())
                   .diaChiKhachHang(h.getKhachHang().getDiaChi());
        }
        
        // ✅ QUAN TRỌNG: Ưu tiên lấy địa chỉ từ ThongTinDonHang (đơn hàng online)
        // Đây là địa chỉ giao hàng chính xác từ checkout
        try {
            Optional<ThongTinDonHang> thongTinDonHang = thongTinDonHangRepository.findByHoaDonId(h.getId());
            if (thongTinDonHang.isPresent()) {
                ThongTinDonHang thongTin = thongTinDonHang.get();
                System.out.println("✅ Found ThongTinDonHang for invoice ID: " + h.getId());
                
                // Map địa chỉ từ ThongTinDonHang
                String diaChiGiaoHang = thongTin.getDiaChiGiaoHang();
                builder.diaChiChiTiet(extractDiaChiChiTiet(diaChiGiaoHang))
                       .tinhThanh(thongTin.getTinhThanh())
                       .quanHuyen(thongTin.getQuanHuyen())
                       .phuongXa(thongTin.getPhuongXa())
                       .diaChiGiaoHang(diaChiGiaoHang);
                
                // ✅ QUAN TRỌNG: Chỉ dùng phiVanChuyen từ ThongTinDonHang nếu phiGiaoHang từ entity là null hoặc 0
                // Ưu tiên: hoa_don.phi_giao_hang > thong_tin_don_hang.phi_van_chuyen
                if (thongTin.getPhiVanChuyen() != null) {
                    java.math.BigDecimal phiVanChuyenFromThongTin = thongTin.getPhiVanChuyen();
                    System.out.println("💰 [toDTO] Found phiVanChuyen in ThongTinDonHang: " + phiVanChuyenFromThongTin);
                    
                    // Chỉ override nếu phiGiaoHang từ entity là null hoặc 0 (chưa có giá trị)
                    if (phiGiaoHang == null || phiGiaoHang.compareTo(java.math.BigDecimal.ZERO) == 0) {
                        phiGiaoHang = phiVanChuyenFromThongTin;
                        builder.phiGiaoHang(phiGiaoHang);
                        System.out.println("💰 [toDTO] Using phiVanChuyen from ThongTinDonHang (phiGiaoHang from entity was null/zero): " + phiGiaoHang);
                        
                        // ✅ QUAN TRỌNG: Tính lại thanhTien với phiGiaoHang từ ThongTinDonHang
                        java.math.BigDecimal recalculatedThanhTien = tongTien.subtract(tienGiamGia).add(phiGiaoHang).max(java.math.BigDecimal.ZERO);
                        builder.thanhTien(recalculatedThanhTien);
                        calculatedThanhTien = recalculatedThanhTien; // Cập nhật giá trị calculatedThanhTien
                        System.out.println("💰 [toDTO] Recalculated thanhTien with phiGiaoHang from ThongTinDonHang: " + recalculatedThanhTien + 
                            " (tongTien: " + tongTien + ", tienGiamGia: " + tienGiamGia + ", phiGiaoHang: " + phiGiaoHang + ")");
                    } else {
                        // Giữ nguyên phiGiaoHang từ entity (đã có giá trị trong DB)
                        System.out.println("💰 [toDTO] Keeping phiGiaoHang from HoaDon entity (hoa_don.phi_giao_hang): " + phiGiaoHang + 
                            " (ignoring ThongTinDonHang.phiVanChuyen: " + phiVanChuyenFromThongTin + ")");
                    }
                } else {
                    System.out.println("💰 [toDTO] No phiVanChuyen in ThongTinDonHang, using phiGiaoHang from entity: " + phiGiaoHang);
                }
                
                System.out.println("✅ Mapped address from ThongTinDonHang: " + thongTin.getDiaChiGiaoHang());
            } else {
                // Fallback: Lấy địa chỉ mặc định của khách hàng từ bảng dia_chi_khach_hang
                if (h.getKhachHang() != null) {
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
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error loading ThongTinDonHang: " + e.getMessage());
            // Fallback to customer address if ThongTinDonHang fails
            if (h.getKhachHang() != null) {
                try {
                    Optional<DiaChiKhachHangDTO> diaChiMacDinh = diaChiKhachHangService.getDiaChiMacDinhByKhachHangId(h.getKhachHang().getId());
                    if (diaChiMacDinh.isPresent()) {
                        DiaChiKhachHangDTO diaChi = diaChiMacDinh.get();
                        builder.diaChiChiTiet(diaChi.getDiaChiChiTiet())
                               .tinhThanh(diaChi.getTinhThanh())
                               .quanHuyen(diaChi.getQuanHuyen())
                               .phuongXa(diaChi.getPhuongXa());
                    }
                } catch (Exception e2) {
                    // Ignore
                }
            }
        }
        
        // Map thông tin nhân viên
        if (h.getNhanVien() != null) {
            builder.tenNhanVien(h.getNhanVien().getHoTen());
        }
        
        // Map viTriBanHang: nhanVienId = null => "Online", có nhanVienId => "Tại quầy"
        String viTriBanHang = (h.getNhanVien() == null) ? "Online" : "Tại quầy";
        builder.viTriBanHang(viTriBanHang);
        
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
        // QUAN TRỌNG: Luôn map danhSachChiTiet, kể cả khi trạng thái là HUY/DA_HUY
        // để frontend có thể hiển thị sản phẩm đã hủy
        List<HoaDonChiTiet> chiTietListToMap = null;
        
        // Ưu tiên 1: Kiểm tra trong entity
        if (h.getDanhSachChiTiet() != null && !h.getDanhSachChiTiet().isEmpty()) {
            System.out.println("📦 Found danhSachChiTiet in entity, count: " + h.getDanhSachChiTiet().size());
            chiTietListToMap = h.getDanhSachChiTiet();
        } else {
            System.out.println("⚠️ No danhSachChiTiet found in entity for invoice ID: " + h.getId() + ", status: " + h.getTrangThai());
            // Ưu tiên 2: Load từ repository (có thể do lazy loading hoặc entity đã detach)
            try {
                System.out.println("🔍 Querying repository for danhSachChiTiet with hoaDonId: " + h.getId());
                List<HoaDonChiTiet> chiTietFromRepo = hoaDonChiTietRepository.findByHoaDonId(h.getId());
                System.out.println("📊 Repository query result: " + (chiTietFromRepo != null ? chiTietFromRepo.size() : "null") + " items");
                
                if (chiTietFromRepo != null && !chiTietFromRepo.isEmpty()) {
                    System.out.println("✅ Found " + chiTietFromRepo.size() + " items in repository, using them");
                    // Log chi tiết item đầu tiên để debug
                    if (chiTietFromRepo.size() > 0) {
                        HoaDonChiTiet firstItem = chiTietFromRepo.get(0);
                        System.out.println("📦 Sample item: id=" + firstItem.getId() + 
                                          ", chiTietSanPhamId=" + (firstItem.getChiTietSanPham() != null ? firstItem.getChiTietSanPham().getId() : "null") +
                                          ", soLuong=" + firstItem.getSoLuong());
                    }
                    chiTietListToMap = chiTietFromRepo;
                    // Set vào entity để lần sau không phải query lại
                    h.setDanhSachChiTiet(chiTietFromRepo);
                } else {
                    System.out.println("⚠️ No danhSachChiTiet found in repository for invoice ID: " + h.getId());
                    System.out.println("   Invoice soLuongSanPham: " + h.getSoLuongSanPham());
                    System.out.println("   Invoice status: " + h.getTrangThai());
                    System.out.println("   Invoice maHoaDon: " + h.getMaHoaDon());
                    
                    // QUAN TRỌNG: Nếu soLuongSanPham > 0 nhưng không tìm thấy danhSachChiTiet,
                    // có thể do dữ liệu không đồng bộ hoặc query có vấn đề
                    // Thử dùng native query để kiểm tra database trực tiếp
                    if (h.getSoLuongSanPham() != null && h.getSoLuongSanPham() > 0) {
                        System.out.println("🔄 soLuongSanPham > 0 (" + h.getSoLuongSanPham() + ") but no danhSachChiTiet found, checking database...");
                        try {
                            // Thử dùng EntityManager để query trực tiếp
                            jakarta.persistence.Query nativeQuery = entityManager.createNativeQuery(
                                "SELECT * FROM hoa_don_chi_tiet WHERE hoa_don_id = :hoaDonId",
                                HoaDonChiTiet.class
                            );
                            nativeQuery.setParameter("hoaDonId", h.getId());
                            @SuppressWarnings("unchecked")
                            List<HoaDonChiTiet> nativeResults = nativeQuery.getResultList();
                            
                            if (nativeResults != null && !nativeResults.isEmpty()) {
                                System.out.println("✅ Found " + nativeResults.size() + " items via native query!");
                                // Load lại với repository để có đầy đủ relationships
                                chiTietListToMap = hoaDonChiTietRepository.findByHoaDonId(h.getId());
                                if (chiTietListToMap == null || chiTietListToMap.isEmpty()) {
                                    // Nếu vẫn không được, thử load từng item
                                    System.out.println("⚠️ Native query found items but repository query failed, trying to load individually...");
                                    chiTietListToMap = new java.util.ArrayList<>();
                                    for (HoaDonChiTiet nativeItem : nativeResults) {
                                        HoaDonChiTiet loadedItem = hoaDonChiTietRepository.findById(nativeItem.getId()).orElse(null);
                                        if (loadedItem != null) {
                                            chiTietListToMap.add(loadedItem);
                                        }
                                    }
                                }
                                if (chiTietListToMap != null && !chiTietListToMap.isEmpty()) {
                                    System.out.println("✅ Successfully loaded " + chiTietListToMap.size() + " items");
                                    h.setDanhSachChiTiet(chiTietListToMap);
                                } else {
                                    System.out.println("❌ Failed to load items even with native query");
                                    builder.danhSachChiTiet(new java.util.ArrayList<>());
                                    chiTietListToMap = null;
                                }
                            } else {
                                System.out.println("❌ No items found in database for hoa_don_id: " + h.getId());
                                System.out.println("   This suggests data inconsistency: soLuongSanPham=" + h.getSoLuongSanPham() + " but no records in hoa_don_chi_tiet table");
                                
                                // Thử query đơn giản hơn (không JOIN FETCH)
                                System.out.println("🔄 Trying simple query without JOIN FETCH...");
                                try {
                                    List<HoaDonChiTiet> simpleResults = hoaDonChiTietRepository.findByHoaDonIdSimple(h.getId());
                                    if (simpleResults != null && !simpleResults.isEmpty()) {
                                        System.out.println("✅ Found " + simpleResults.size() + " items with simple query!");
                                        chiTietListToMap = simpleResults;
                                        h.setDanhSachChiTiet(simpleResults);
                                    } else {
                                        builder.danhSachChiTiet(new java.util.ArrayList<>());
                                        chiTietListToMap = null;
                                    }
                                } catch (Exception e3) {
                                    System.err.println("❌ Error in simple query: " + e3.getMessage());
                                    builder.danhSachChiTiet(new java.util.ArrayList<>());
                                    chiTietListToMap = null;
                                }
                            }
                        } catch (Exception e2) {
                            System.err.println("❌ Error in native query: " + e2.getMessage());
                            e2.printStackTrace();
                            builder.danhSachChiTiet(new java.util.ArrayList<>());
                            chiTietListToMap = null;
                        }
                    } else {
                        // Set empty list thay vì null để frontend có thể xử lý
                        builder.danhSachChiTiet(new java.util.ArrayList<>());
                        chiTietListToMap = null; // Đánh dấu là không có gì
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading danhSachChiTiet from repository: " + e.getMessage());
                e.printStackTrace();
                // Set empty list thay vì null để frontend có thể xử lý
                builder.danhSachChiTiet(new java.util.ArrayList<>());
                chiTietListToMap = null; // Đánh dấu là không có gì
            }
        }
        
        // QUAN TRỌNG: Map sang DTO - luôn set danhSachChiTiet, kể cả khi empty
        if (chiTietListToMap != null && !chiTietListToMap.isEmpty()) {
            System.out.println("📦 Mapping " + chiTietListToMap.size() + " danhSachChiTiet items to DTO...");
            List<HoaDonChiTietDTO> chiTietDTOList = chiTietListToMap.stream()
                    .map(this::toChiTietDTO)
                    .collect(Collectors.toList());
            builder.danhSachChiTiet(chiTietDTOList);
            System.out.println("✅ Mapped danhSachChiTiet, DTO count: " + chiTietDTOList.size());
        } else {
            // QUAN TRỌNG: Nếu không có dữ liệu, vẫn set empty list để frontend có thể xử lý
            // Không để null vì frontend có thể check length
            System.out.println("⚠️ No danhSachChiTiet to map, setting empty list for invoice ID: " + h.getId());
            builder.danhSachChiTiet(new java.util.ArrayList<>());
        }
        
        HoaDonDTO result = builder.build();
        System.out.println("📤 Final DTO - Invoice ID: " + result.getId() + ", danhSachChiTiet size: " + 
                          (result.getDanhSachChiTiet() != null ? result.getDanhSachChiTiet().size() : "null"));
        return result;
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
                       .maSanPham(sanPham.getMaSanPham());
                String anhBienThe = chiTietSP.getAnhSanPham();
                if (StringUtils.hasText(anhBienThe)) {
                    builder.anhSanPham(anhBienThe);
                } else {
                    builder.anhSanPham(sanPham.getAnhSanPham());
                }
                
                // Lấy nhà sản xuất
                if (sanPham.getNhaSanXuat() != null) {
                    builder.nhaSanXuat(sanPham.getNhaSanXuat().getTenNhaSanXuat());
                }
                
                // Lấy loại mũ bảo hiểm (danh mục)
                if (sanPham.getLoaiMuBaoHiem() != null) {
                    builder.danhMuc(sanPham.getLoaiMuBaoHiem().getTenLoai());
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

        // Xử lý ghi chú: Cho phép cả null và empty string (để có thể xóa ghi chú)
        // Nhưng nếu có giá trị thì set vào
        if (d.getGhiChu() != null) {
            System.out.println("📝 Setting ghiChu: '" + d.getGhiChu() + "' (length: " + d.getGhiChu().length() + ")");
            h.setGhiChu(d.getGhiChu());
        } else {
            System.out.println("⚠️ ghiChu is null in DTO, keeping existing value: '" + h.getGhiChu() + "'");
        }

        if (d.getTrangThai() != null) {
            // Convert String từ DTO sang enum, map "HUY" -> "DA_HUY"
            HoaDon.TrangThaiHoaDon trangThaiEnum = convertStringToTrangThaiEnum(d.getTrangThai());
            System.out.println("🔄 Setting trangThai: " + d.getTrangThai() + " -> " + trangThaiEnum);
            h.setTrangThai(trangThaiEnum);
        }
        if (d.getSoLuongSanPham() != null) h.setSoLuongSanPham(d.getSoLuongSanPham());
        // ✅ QUAN TRỌNG: Luôn cập nhật phiGiaoHang nếu có trong DTO (kể cả khi = 0)
        if (d.getPhiGiaoHang() != null) {
            h.setPhiGiaoHang(d.getPhiGiaoHang());
            System.out.println("💰 Setting phiGiaoHang in HoaDon entity: " + d.getPhiGiaoHang());
        } else {
            System.out.println("⚠️ phiGiaoHang is null in DTO, keeping existing value: " + h.getPhiGiaoHang());
        }
        
        // ✅ CHO PHÉP HÓA ĐƠN KHÔNG CÓ KHÁCH HÀNG (BÁN HÀNG TẠI QUẦY)
        KhachHang attachedCustomer = resolveCustomerForInvoice(d);
        if (attachedCustomer != null) {
            h.setKhachHang(attachedCustomer);
            System.out.println("✅ Set khachHang ID: " + attachedCustomer.getId());
        } else {
            // Cho phép khách hàng là null
            h.setKhachHang(null);
            System.out.println("✅ Set khachHang = null (counter sale without customer - ALLOWED)");
        }
        
        // Map nhân viên từ ID
        // QUAN TRỌNG: Nếu nhanVienId là null (đơn hàng online), phải set nhanVien = null
        // Nếu nhanVienId không null (đơn hàng tại quầy), load và set nhanVien
        if (d.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(d.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + d.getNhanVienId()));
            h.setNhanVien(nhanVien);
            System.out.println("🏪 Counter order - Set nhanVien ID: " + d.getNhanVienId());
        } else {
            // Đơn hàng online - đảm bảo nhanVien = null
            h.setNhanVien(null);
            System.out.println("🌐 Online order - Set nhanVien = null");
        }
    }

    private KhachHang resolveCustomerForInvoice(HoaDonDTO dto) {
        if (dto == null) {
            return null;
        }

        if (dto.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(dto.getKhachHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + dto.getKhachHangId()));
            updateCustomerProfileFromOrder(khachHang, dto, true, true);
            updatePurchaseStats(khachHang);
            return khachHangRepository.save(khachHang);
        }

        String name = sanitize(dto.getTenKhachHang());
        String email = sanitize(dto.getEmailKhachHang());
        String phone = sanitize(dto.getSoDienThoaiKhachHang());

        // Cho phép tạo hóa đơn tại quầy mà không cần thông tin khách hàng
        // Nếu không có thông tin khách hàng, trả về null
        if (!StringUtils.hasText(name) && !StringUtils.hasText(phone) && !StringUtils.hasText(email)) {
            return null;
        }

        Optional<KhachHang> existing = Optional.empty();
        boolean matchedByEmail = false;
        boolean matchedByPhone = false;

        if (StringUtils.hasText(email)) {
            existing = khachHangRepository.findByEmail(email);
            matchedByEmail = existing.isPresent();
        }

        if (existing.isEmpty() && StringUtils.hasText(phone)) {
            existing = khachHangRepository.findBySoDienThoai(phone);
            matchedByPhone = existing.isPresent();
        }

        if (existing.isPresent()) {
            KhachHang khachHang = existing.get();
            updateCustomerProfileFromOrder(khachHang, dto, matchedByEmail, matchedByPhone);
            updatePurchaseStats(khachHang);
            return khachHangRepository.save(khachHang);
        }

        // Chỉ tạo khách hàng mới nếu có ít nhất một thông tin (name, email, hoặc phone)
        // Nếu không có thông tin nào, trả về null (cho phép hóa đơn tại quầy không cần khách hàng)
        if (!StringUtils.hasText(name) && !StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            return null;
        }

        KhachHang newKhachHang = new KhachHang();
        newKhachHang.setMaKhachHang(generateUniqueCustomerCode());
        // Chỉ set các field có giá trị
        if (StringUtils.hasText(name)) {
            newKhachHang.setTenKhachHang(name);
        }
        if (StringUtils.hasText(email)) {
            newKhachHang.setEmail(email);
        }
        if (StringUtils.hasText(phone)) {
            newKhachHang.setSoDienThoai(phone);
        }
        String address = sanitize(dto.getDiaChiChiTiet());
        if (StringUtils.hasText(address)) {
            newKhachHang.setDiaChi(address);
        }
        newKhachHang.setTrangThai(true);
        newKhachHang.setNgayTao(LocalDate.now());
        newKhachHang.setSoLanMua(0);
        updatePurchaseStats(newKhachHang);
        return khachHangRepository.save(newKhachHang);
    }

    private void updateCustomerProfileFromOrder(KhachHang khachHang, HoaDonDTO dto, boolean matchedByEmail, boolean matchedByPhone) {
        if (khachHang == null || dto == null) {
            return;
        }

        String name = sanitize(dto.getTenKhachHang());
        if (StringUtils.hasText(name)) {
            khachHang.setTenKhachHang(name);
        }

        String email = sanitize(dto.getEmailKhachHang());
        if (StringUtils.hasText(email)) {
            if (matchedByEmail || !StringUtils.hasText(khachHang.getEmail())) {
                khachHang.setEmail(email);
            }
        }

        // ✅ CHO PHÉP CẬP NHẬT HOẶC KHÔNG CẬP NHẬT SỐ ĐIỆN THOẠI
        // - Nếu DTO có soDienThoaiKhachHang và có giá trị (không null, không empty) → Cập nhật
        // - Nếu DTO không có soDienThoaiKhachHang hoặc null/empty → Giữ nguyên số điện thoại cũ
        String phone = sanitize(dto.getSoDienThoaiKhachHang());
        if (StringUtils.hasText(phone)) {
            // Có giá trị trong DTO → Cập nhật số điện thoại
            // Cho phép cập nhật ngay cả khi khách hàng đã có số điện thoại khác
                khachHang.setSoDienThoai(phone);
            System.out.println("📱 Updating customer phone number: " + phone);
        } else {
            // Không có giá trị trong DTO → Giữ nguyên số điện thoại cũ
            System.out.println("📱 Keeping existing customer phone number: " + khachHang.getSoDienThoai());
        }

        String address = sanitize(dto.getDiaChiChiTiet());
        if (StringUtils.hasText(address) && !StringUtils.hasText(khachHang.getDiaChi())) {
            khachHang.setDiaChi(address);
        }
    }

    private void updatePurchaseStats(KhachHang khachHang) {
        if (khachHang == null) {
            return;
        }
        int soLanMua = khachHang.getSoLanMua() != null ? khachHang.getSoLanMua() : 0;
        khachHang.setSoLanMua(soLanMua + 1);
        khachHang.setLanMuaGanNhat(LocalDate.now());
    }

    private String generateUniqueCustomerCode() {
        String code;
        int attempts = 0;
        do {
            long randomPart = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
            code = "KH" + randomPart;
            attempts++;
        } while (khachHangRepository.existsByMaKhachHang(code) && attempts < 5);

        if (khachHangRepository.existsByMaKhachHang(code)) {
            code = "KH" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
        }
        return code;
    }

    private String sanitize(String value) {
        return value != null ? value.trim() : null;
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
            java.math.BigDecimal recalculatedTotal = java.math.BigDecimal.ZERO;
            
            for (HoaDonChiTietDTO chiTietDTO : dto.getDanhSachChiTiet()) {
                if (chiTietDTO.getChiTietSanPhamId() == null) {
                    System.err.println("⚠️ Skipping chiTiet with null chiTietSanPhamId");
                    continue; // Bỏ qua nếu không có chiTietSanPhamId
                }
                
                System.out.println("🔍 Processing chiTietSanPhamId: " + chiTietDTO.getChiTietSanPhamId() + ", soLuong: " + chiTietDTO.getSoLuong());
                
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
                        .orElseThrow(() -> {
                            String errorMsg = "Không tìm thấy chi tiết sản phẩm với ID: " + chiTietDTO.getChiTietSanPhamId();
                            System.err.println("❌ " + errorMsg);
                            return new RuntimeException(errorMsg);
                        });
                
                // QUAN TRỌNG: Kiểm tra tồn kho trước khi tạo hóa đơn
                int requestedQuantity = chiTietDTO.getSoLuong() != null ? chiTietDTO.getSoLuong() : 0;
                int currentStock = 0;
                try {
                    if (chiTietSanPham.getSoLuongTon() != null && !chiTietSanPham.getSoLuongTon().trim().isEmpty()) {
                        currentStock = Integer.parseInt(chiTietSanPham.getSoLuongTon());
                    } else {
                        System.err.println("⚠️ soLuongTon is null or empty for ChiTietSanPham id: " + chiTietSanPham.getId() + ", defaulting to 0");
                        currentStock = 0;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid stock quantity format for ChiTietSanPham id: " + chiTietSanPham.getId() + ", value: " + chiTietSanPham.getSoLuongTon());
                    currentStock = 0;
                }
                
                System.out.println("📦 Stock check - ChiTietSanPham ID: " + chiTietSanPham.getId() + ", currentStock: " + currentStock + ", requested: " + requestedQuantity);
                
                if (requestedQuantity > currentStock) {
                    String errorMsg = String.format("Sản phẩm \"%s\" chỉ còn %d sản phẩm trong kho (bạn yêu cầu %d).", 
                            chiTietDTO.getTenSanPham() != null ? chiTietDTO.getTenSanPham() : "N/A",
                            currentStock, requestedQuantity);
                    System.err.println("❌ " + errorMsg);
                    throw new RuntimeException(errorMsg);
                }
                
                // Tính lại giá từ backend (có thể đã thay đổi)
                java.math.BigDecimal currentPrice = chiTietDTO.getDonGia();
                if (currentPrice == null && chiTietSanPham.getGiaBan() != null) {
                    try {
                        currentPrice = new java.math.BigDecimal(chiTietSanPham.getGiaBan());
                    } catch (NumberFormatException e) {
                        currentPrice = java.math.BigDecimal.ZERO;
                    }
                }
                if (currentPrice == null) {
                    currentPrice = java.math.BigDecimal.ZERO;
                }
                
                java.math.BigDecimal discount = chiTietDTO.getGiamGia() != null ? chiTietDTO.getGiamGia() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal itemTotal = currentPrice.multiply(java.math.BigDecimal.valueOf(requestedQuantity))
                    .subtract(discount).max(java.math.BigDecimal.ZERO);
                
                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHoaDon(h);
                hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
                hoaDonChiTiet.setSoLuong(requestedQuantity);
                hoaDonChiTiet.setDonGia(currentPrice);
                hoaDonChiTiet.setGiamGia(discount);
                hoaDonChiTiet.setThanhTien(itemTotal);
                
                chiTietList.add(hoaDonChiTiet);
                recalculatedTotal = recalculatedTotal.add(itemTotal);
                
                // QUAN TRỌNG: Kiểm tra tồn kho khi tạo hóa đơn
                System.out.println("✅ Verified stock for ChiTietSanPham id: " + chiTietSanPham.getId() + 
                    " - current stock: " + currentStock + ", requested: " + requestedQuantity);
            }
            // QUAN TRỌNG: Set danhSachChiTiet trước khi save để JPA cascade save đúng
            h.setDanhSachChiTiet(chiTietList);
            h.setSoLuongSanPham(chiTietList.size());
            
            // Tính lại tổng tiền từ danh sách chi tiết
            if (h.getTongTien() == null || h.getTongTien().compareTo(recalculatedTotal) != 0) {
                System.out.println("⚠️ Recalculated total: " + h.getTongTien() + " -> " + recalculatedTotal);
                h.setTongTien(recalculatedTotal);
            }
            
            // ✅ QUAN TRỌNG: Tính lại thanhTien = tongTien - tienGiamGia + phiGiaoHang
            java.math.BigDecimal phiGiaoHang = h.getPhiGiaoHang() != null ? h.getPhiGiaoHang() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal tienGiamGia = h.getTienGiamGia() != null ? h.getTienGiamGia() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal tongTien = h.getTongTien() != null ? h.getTongTien() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal calculatedThanhTien = tongTien.subtract(tienGiamGia).add(phiGiaoHang).max(java.math.BigDecimal.ZERO);
            
            if (h.getThanhTien() == null || h.getThanhTien().compareTo(calculatedThanhTien) != 0) {
                System.out.println("💰 Recalculating thanhTien: " + h.getThanhTien() + " -> " + calculatedThanhTien + 
                    " (tongTien: " + tongTien + ", tienGiamGia: " + tienGiamGia + ", phiGiaoHang: " + phiGiaoHang + ")");
                h.setThanhTien(calculatedThanhTien);
            }
            
            System.out.println("📦 Prepared " + chiTietList.size() + " HoaDonChiTiet items to save with invoice");
        } else {
            System.out.println("⚠️ No danhSachChiTiet provided in DTO");
            h.setSoLuongSanPham(0);
        }
        
        // Lưu hóa đơn (với cascade, danhSachChiTiet sẽ được lưu tự động)
        HoaDon saved = hoaDonRepository.save(h);
        hoaDonRepository.flush(); // Force flush để đảm bảo danhSachChiTiet được lưu ngay
        
        // Log activity: CREATE
        try {
            String newDataJson = serializeHoaDonToJson(saved);
            hoaDonActivityService.logActivity(
                saved,
                "CREATE",
                String.format("Tạo hóa đơn mới: %s - Tổng tiền: %s VNĐ", 
                    saved.getMaHoaDon(), 
                    saved.getThanhTien() != null ? saved.getThanhTien().toString() : "0"),
                null,
                newDataJson
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to log CREATE activity: " + e.getMessage());
        }
        
        // Verify danhSachChiTiet đã được lưu
        List<HoaDonChiTiet> savedChiTietList = null;
        if (saved.getDanhSachChiTiet() != null) {
            System.out.println("✅ Saved invoice with " + saved.getDanhSachChiTiet().size() + " danhSachChiTiet items");
            savedChiTietList = saved.getDanhSachChiTiet();
        } else {
            System.err.println("❌ WARNING: danhSachChiTiet is null after save! Invoice ID: " + saved.getId());
            // Thử load lại từ repository
            savedChiTietList = hoaDonChiTietRepository.findByHoaDonId(saved.getId());
            if (savedChiTietList != null && !savedChiTietList.isEmpty()) {
                System.out.println("✅ Found " + savedChiTietList.size() + " items in repository after save");
                saved.setDanhSachChiTiet(savedChiTietList);
            }
        }
        
        // QUAN TRỌNG: Logic trừ số lượng
        // 1. Đơn hàng ONLINE (nhanVienId = null): Trừ stock ngay khi đặt hàng thành công (khi tạo đơn), KHÔNG cần chờ admin xác nhận
        // 2. Đơn hàng TẠI QUẦY (nhanVienId != null): KHÔNG trừ stock khi tạo đơn, chỉ trừ khi admin xác nhận (DA_XAC_NHAN)
        // Lý do: 
        //   - Online: Khách hàng đặt hàng = đã thanh toán thành công, trừ stock ngay
        //   - Tại quầy: Chưa thanh toán, chỉ trừ khi admin xác nhận (thanh toán thành công)
        
        // Debug: Log thông tin để kiểm tra
        System.out.println("🔍 Checking order type for stock deduction:");
        System.out.println("   - saved.getNhanVien(): " + (saved.getNhanVien() != null ? "ID=" + saved.getNhanVien().getId() : "null"));
        System.out.println("   - savedChiTietList: " + (savedChiTietList != null ? savedChiTietList.size() + " items" : "null"));
        System.out.println("   - Order status: " + saved.getTrangThai());
        
        if (saved.getNhanVien() == null) {
            // Đơn hàng ONLINE - trừ stock ngay khi đặt hàng thành công (khi tạo đơn)
            if (savedChiTietList != null && !savedChiTietList.isEmpty()) {
                System.out.println("🌐 Online order detected (nhanVienId = null) - Deducting stock immediately (order placed successfully)...");
                System.out.println("   - Number of items to deduct: " + savedChiTietList.size());
                deductStockFromInvoice(savedChiTietList);
            } else {
                System.err.println("❌ WARNING: Online order but savedChiTietList is null or empty! Cannot deduct stock.");
                System.err.println("   - savedChiTietList is null: " + (savedChiTietList == null));
                System.err.println("   - savedChiTietList is empty: " + (savedChiTietList != null && savedChiTietList.isEmpty()));
            }
        } else {
            // Đơn hàng TẠI QUẦY - KHÔNG trừ stock khi tạo đơn, sẽ trừ khi admin xác nhận (DA_XAC_NHAN)
            System.out.println("🏪 Counter order detected (nhanVienId = " + saved.getNhanVien().getId() + 
                ") - Stock will be deducted when status changes to DA_XAC_NHAN (confirmed)");
        }
        
        // Xử lý địa chỉ giao hàng: Nếu có địa chỉ từ DTO (checkout), tạo hoặc cập nhật địa chỉ khách hàng
        // Lấy khachHangId từ hóa đơn đã lưu (có thể đã tạo mới)
        Long khachHangId = saved.getKhachHang() != null ? saved.getKhachHang().getId() : null;
        if (khachHangId != null && 
            (dto.getDiaChiChiTiet() != null || dto.getTinhThanh() != null || 
             dto.getQuanHuyen() != null || dto.getPhuongXa() != null)) {
            try {
                // Kiểm tra xem địa chỉ này đã tồn tại chưa
                Optional<DiaChiKhachHangDTO> diaChiMacDinh = diaChiKhachHangService.getDiaChiMacDinhByKhachHangId(khachHangId);
                
                // Tạo DTO cho địa chỉ mới
                DiaChiKhachHangDTO diaChiDTO = new DiaChiKhachHangDTO();
                diaChiDTO.setKhachHangId(khachHangId);
                diaChiDTO.setTenNguoiNhan(dto.getTenKhachHang() != null ? dto.getTenKhachHang() : "");
                diaChiDTO.setSoDienThoai(dto.getSoDienThoaiKhachHang() != null ? dto.getSoDienThoaiKhachHang() : "");
                diaChiDTO.setDiaChiChiTiet(dto.getDiaChiChiTiet() != null ? dto.getDiaChiChiTiet() : "");
                diaChiDTO.setTinhThanh(dto.getTinhThanh() != null ? dto.getTinhThanh() : "");
                diaChiDTO.setQuanHuyen(dto.getQuanHuyen() != null ? dto.getQuanHuyen() : "");
                diaChiDTO.setPhuongXa(dto.getPhuongXa() != null ? dto.getPhuongXa() : "");
                
                // Nếu chưa có địa chỉ mặc định, đặt làm mặc định
                if (!diaChiMacDinh.isPresent()) {
                    diaChiDTO.setMacDinh(true);
                } else {
                    diaChiDTO.setMacDinh(false);
                }
                
                // Tạo địa chỉ mới
                diaChiKhachHangService.createDiaChi(diaChiDTO);
                System.out.println("✅ Created delivery address for customer ID: " + khachHangId);
            } catch (Exception e) {
                // Không block việc tạo hóa đơn nếu lưu địa chỉ thất bại
                System.err.println("⚠️ Could not save delivery address: " + e.getMessage());
            }
        }
        
        // QUAN TRỌNG: Tạo ThongTinDonHang cho đơn hàng online
        // ThongTinDonHang chứa thông tin giao hàng chi tiết
        if (dto.getTenKhachHang() != null && dto.getSoDienThoaiKhachHang() != null &&
            dto.getDiaChiChiTiet() != null && dto.getTinhThanh() != null) {
            try {
                ThongTinDonHang thongTinDonHang = new ThongTinDonHang();
                thongTinDonHang.setHoaDon(saved);
                thongTinDonHang.setTenNguoiNhan(dto.getTenKhachHang());
                thongTinDonHang.setSoDienThoai(dto.getSoDienThoaiKhachHang());
                
                // Tạo địa chỉ giao hàng đầy đủ
                String diaChiGiaoHang = String.format("%s, %s, %s, %s",
                    dto.getDiaChiChiTiet() != null ? dto.getDiaChiChiTiet() : "",
                    dto.getPhuongXa() != null ? dto.getPhuongXa() : "",
                    dto.getQuanHuyen() != null ? dto.getQuanHuyen() : "",
                    dto.getTinhThanh() != null ? dto.getTinhThanh() : "");
                thongTinDonHang.setDiaChiGiaoHang(diaChiGiaoHang.trim().replaceAll("^,\\s*|,\\s*$", ""));
                thongTinDonHang.setTinhThanh(dto.getTinhThanh() != null ? dto.getTinhThanh() : "");
                thongTinDonHang.setQuanHuyen(dto.getQuanHuyen() != null ? dto.getQuanHuyen() : "");
                thongTinDonHang.setPhuongXa(dto.getPhuongXa() != null ? dto.getPhuongXa() : "");
                thongTinDonHang.setGhiChu(dto.getGhiChu());
                
                // ✅ QUAN TRỌNG: Set phí vận chuyển khi tạo ThongTinDonHang từ checkout
                if (dto.getPhiGiaoHang() != null) {
                    thongTinDonHang.setPhiVanChuyen(dto.getPhiGiaoHang());
                    System.out.println("💰 Setting ThongTinDonHang phiVanChuyen to: " + dto.getPhiGiaoHang());
                }
                
                thongTinDonHangRepository.save(thongTinDonHang);
                System.out.println("✅ Created ThongTinDonHang for invoice ID: " + saved.getId() + 
                    " with address: " + diaChiGiaoHang);
            } catch (Exception e) {
                // Không block việc tạo hóa đơn nếu tạo ThongTinDonHang thất bại
                System.err.println("⚠️ Could not save ThongTinDonHang: " + e.getMessage());
            }
        }
        
        // ✅ Xử lý danh sách phiếu giảm giá nếu có
        if (dto.getPhieuGiamGiaIds() != null && !dto.getPhieuGiamGiaIds().isEmpty()) {
            // Lưu danh sách ID phiếu giảm giá vào ghiChu dưới dạng JSON
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String phieuGiamGiaIdsJson = objectMapper.writeValueAsString(dto.getPhieuGiamGiaIds());
                String existingGhiChu = saved.getGhiChu() != null ? saved.getGhiChu() : "";
                String newGhiChu = existingGhiChu.isEmpty() 
                    ? "PHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson
                    : existingGhiChu + "\nPHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson;
                saved.setGhiChu(newGhiChu);
                System.out.println("✅ Saved phieuGiamGiaIds to ghiChu: " + phieuGiamGiaIdsJson);
            } catch (Exception e) {
                System.err.println("❌ Error saving phieuGiamGiaIds: " + e.getMessage());
            }
        } else if (dto.getPhieuGiamGiaId() != null) {
            // Giữ tương thích với phieuGiamGiaId cũ
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String phieuGiamGiaIdsJson = objectMapper.writeValueAsString(java.util.Arrays.asList(dto.getPhieuGiamGiaId()));
                String existingGhiChu = saved.getGhiChu() != null ? saved.getGhiChu() : "";
                String newGhiChu = existingGhiChu.isEmpty() 
                    ? "PHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson
                    : existingGhiChu + "\nPHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson;
                saved.setGhiChu(newGhiChu);
                System.out.println("✅ Saved phieuGiamGiaId (legacy) to ghiChu: " + phieuGiamGiaIdsJson);
            } catch (Exception e) {
                System.err.println("❌ Error saving phieuGiamGiaId: " + e.getMessage());
            }
        }
        
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
            // ✅ QUAN TRỌNG: Chỉ set trạng thái "Đã Thanh Toán" khi phương thức thanh toán là chuyển khoản
            // Với tiền mặt, trạng thái sẽ là CHUA_THANH_TOAN (mặc định)
            if ("Chuyển khoản".equals(tenHinhThuc) || "transfer".equalsIgnoreCase(phuongThucTen)) {
                pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN);
                System.out.println("✅ Payment method is transfer - Set status to DA_THANH_TOAN");
            } else {
                pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.CHO_THANH_TOAN);
                System.out.println("✅ Payment method is cash - Set status to CHO_THANH_TOAN");
            }
            phuongThucThanhToanRepository.save(pttt);
        }
        
        // Reload hóa đơn bằng cách gọi getHoaDonById để đảm bảo có dữ liệu đầy đủ
        // Lưu ý: Không fetch phuongThucThanhToan trong query để tránh MultipleBagFetchException
        Optional<HoaDon> reloaded = getHoaDonById(saved.getId());
        
        // Verify danhSachChiTiet trong reloaded invoice
        if (reloaded.isPresent()) {
            HoaDon reloadedHoaDon = reloaded.get();
            if (reloadedHoaDon.getDanhSachChiTiet() != null && !reloadedHoaDon.getDanhSachChiTiet().isEmpty()) {
                System.out.println("✅ Reloaded invoice has " + reloadedHoaDon.getDanhSachChiTiet().size() + " danhSachChiTiet items");
            } else {
                System.err.println("❌ WARNING: Reloaded invoice has empty danhSachChiTiet! Invoice ID: " + reloadedHoaDon.getId());
            }
        }
        
        HoaDonDTO resultDTO;
        if (reloaded.isPresent()) {
            resultDTO = toDTO(reloaded.get());
        } else {
            resultDTO = toDTO(saved);
        }
        
        // Gửi email thông báo hóa đơn cho khách hàng
        try {
            sendInvoiceEmailNotification(resultDTO, reloaded.isPresent() ? reloaded.get() : saved);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send invoice email notification: " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến việc tạo hóa đơn
        }
        
        return resultDTO;
    }

    @Transactional
    public HoaDonDTO updateHoaDon(Long id, HoaDonDTO dto) {

        HoaDon h = getHoaDonById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        
        // Lưu dữ liệu cũ để log
        String oldDataJson = null;
        try {
            oldDataJson = serializeHoaDonToJson(h);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to serialize old data: " + e.getMessage());
        }
        
        // ✅ QUAN TRỌNG: Lưu địa chỉ cũ để so sánh và gửi email nếu có thay đổi
        String oldAddress = null;
        try {
            // Lấy địa chỉ cũ từ ThongTinDonHang hoặc từ entity
            Optional<ThongTinDonHang> oldThongTin = thongTinDonHangRepository.findByHoaDonId(id);
            if (oldThongTin.isPresent()) {
                oldAddress = oldThongTin.get().getDiaChiGiaoHang();
            } else if (h.getKhachHang() != null && h.getKhachHang().getDiaChi() != null) {
                oldAddress = h.getKhachHang().getDiaChi();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get old address: " + e.getMessage());
        }
        
        updateEntityFromDTO(h, dto);
        
        // Xử lý danh sách chi tiết sản phẩm nếu có
        // QUAN TRỌNG: Với orphanRemoval = true, KHÔNG được set collection mới hoặc clear() mà không add lại ngay
        // Giải pháp: Chỉ clear và add lại khi có danhSachChiTiet mới (không empty)
        // Nếu danhSachChiTiet là null hoặc empty, giữ nguyên collection hiện tại
        System.out.println("📦 Processing danhSachChiTiet in updateHoaDon:");
        System.out.println("   - dto.getDanhSachChiTiet() is null: " + (dto.getDanhSachChiTiet() == null));
        System.out.println("   - dto.getDanhSachChiTiet() size: " + (dto.getDanhSachChiTiet() != null ? dto.getDanhSachChiTiet().size() : "null"));
        System.out.println("   - Current h.getDanhSachChiTiet() size: " + (h.getDanhSachChiTiet() != null ? h.getDanhSachChiTiet().size() : "null"));

        // QUAN TRỌNG: Đảm bảo danhSachChiTiet được load đầy đủ từ database trước khi xử lý
        // Nếu collection hiện tại là null hoặc empty nhưng có dữ liệu trong DB, load lại
        if ((h.getDanhSachChiTiet() == null || h.getDanhSachChiTiet().isEmpty()) && dto.getDanhSachChiTiet() == null) {
            System.out.println("🔄 danhSachChiTiet is null/empty in entity but DTO also null - Loading from repository...");
            List<HoaDonChiTiet> existingChiTiet = hoaDonChiTietRepository.findByHoaDonId(id);
            if (existingChiTiet != null && !existingChiTiet.isEmpty()) {
                System.out.println("✅ Found " + existingChiTiet.size() + " existing items in DB, setting to entity");
                h.setDanhSachChiTiet(existingChiTiet);
            }
        }

        // Chỉ xử lý danhSachChiTiet nếu có dữ liệu mới (không null và không empty)
        // Nếu null hoặc empty, giữ nguyên collection hiện tại để không mất dữ liệu
        if (dto.getDanhSachChiTiet() != null && !dto.getDanhSachChiTiet().isEmpty()) {
            System.out.println("✅ Updating danhSachChiTiet with " + dto.getDanhSachChiTiet().size() + " items");

            // Đảm bảo collection được khởi tạo trước
            if (h.getDanhSachChiTiet() == null) {
                h.setDanhSachChiTiet(new ArrayList<>());
            }

            // Xóa các chi tiết cũ bằng cách clear() collection
            // Với orphanRemoval = true, clear() sẽ tự động xóa các item khỏi database
            h.getDanhSachChiTiet().clear();

            // Thêm các chi tiết mới ngay lập tức
            for (HoaDonChiTietDTO chiTietDTO : dto.getDanhSachChiTiet()) {
                if (chiTietDTO.getChiTietSanPhamId() == null) {
                    System.out.println("⚠️ Skipping item with null chiTietSanPhamId");
                    continue; // Bỏ qua nếu không có chiTietSanPhamId
                }
                
                // Load ChiTietSanPham từ database
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietDTO.getChiTietSanPhamId()));
                
                // Tạo HoaDonChiTiet mới
                HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                hoaDonChiTiet.setHoaDon(h);
                hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
                hoaDonChiTiet.setSoLuong(chiTietDTO.getSoLuong() != null ? chiTietDTO.getSoLuong() : 0);
                hoaDonChiTiet.setDonGia(chiTietDTO.getDonGia() != null ? chiTietDTO.getDonGia() : java.math.BigDecimal.ZERO);
                hoaDonChiTiet.setGiamGia(chiTietDTO.getGiamGia() != null ? chiTietDTO.getGiamGia() : java.math.BigDecimal.ZERO);
                hoaDonChiTiet.setThanhTien(chiTietDTO.getThanhTien() != null ? chiTietDTO.getThanhTien() : java.math.BigDecimal.ZERO);
                
                // QUAN TRỌNG: Add ngay vào collection sau khi clear (không được delay)
                // Với orphanRemoval = true, phải add ngay sau khi clear để không mất dữ liệu
                h.getDanhSachChiTiet().add(hoaDonChiTiet);
                System.out.println("✅ Added chiTiet: chiTietSanPhamId=" + chiTietDTO.getChiTietSanPhamId() + ", soLuong=" + hoaDonChiTiet.getSoLuong());
            }

            System.out.println("✅ Added " + h.getDanhSachChiTiet().size() + " items to danhSachChiTiet");
        } else {
            // Nếu danhSachChiTiet là null hoặc empty trong DTO, giữ nguyên collection hiện tại
            // Điều này đảm bảo không mất dữ liệu khi chỉ cập nhật trạng thái hoặc ghi chú
            // QUAN TRỌNG: Đảm bảo collection không bị null trước khi save
            if (h.getDanhSachChiTiet() == null) {
                System.out.println("⚠️ danhSachChiTiet is null in entity, loading from repository to prevent deletion...");
                List<HoaDonChiTiet> existingChiTiet = hoaDonChiTietRepository.findByHoaDonId(id);
                if (existingChiTiet != null && !existingChiTiet.isEmpty()) {
                    System.out.println("✅ Loaded " + existingChiTiet.size() + " items from repository");
                    h.setDanhSachChiTiet(existingChiTiet);
                } else {
                    System.out.println("⚠️ No existing items found in repository, initializing empty list");
                    h.setDanhSachChiTiet(new ArrayList<>());
                }
            } else {
                System.out.println("✅ Keeping existing danhSachChiTiet collection (size: " + h.getDanhSachChiTiet().size() + ")");
            }
        }
        
        System.out.println("💾 Saving invoice with ghiChu: '" + h.getGhiChu() + "'");

        // ✅ QUAN TRỌNG: Tính lại thanhTien sau khi cập nhật tất cả thông tin
        // thanhTien = tongTien - tienGiamGia + phiGiaoHang
        java.math.BigDecimal tongTien = h.getTongTien() != null ? h.getTongTien() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal tienGiamGia = h.getTienGiamGia() != null ? h.getTienGiamGia() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal phiGiaoHang = h.getPhiGiaoHang() != null ? h.getPhiGiaoHang() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal calculatedThanhTien = tongTien.subtract(tienGiamGia).add(phiGiaoHang).max(java.math.BigDecimal.ZERO);
        
        if (h.getThanhTien() == null || h.getThanhTien().compareTo(calculatedThanhTien) != 0) {
            System.out.println("💰 Recalculating thanhTien in updateHoaDon: " + h.getThanhTien() + " -> " + calculatedThanhTien + 
                " (tongTien: " + tongTien + ", tienGiamGia: " + tienGiamGia + ", phiGiaoHang: " + phiGiaoHang + ")");
            h.setThanhTien(calculatedThanhTien);
        }
        
        // ✅ QUAN TRỌNG: Log trước khi save để kiểm tra dữ liệu
        System.out.println("💾 Before saving to DB:");
        System.out.println("   - phiGiaoHang: " + h.getPhiGiaoHang());
        System.out.println("   - tongTien: " + h.getTongTien());
        System.out.println("   - tienGiamGia: " + h.getTienGiamGia());
        System.out.println("   - thanhTien: " + h.getThanhTien());

        // Lưu hóa đơn
        HoaDon saved = hoaDonRepository.save(h);
        hoaDonRepository.flush(); // Force flush để đảm bảo dữ liệu được lưu ngay
        
        // ✅ QUAN TRỌNG: Log sau khi save để xác nhận dữ liệu đã được lưu
        System.out.println("✅ After saving to DB:");
        System.out.println("   - phiGiaoHang: " + saved.getPhiGiaoHang());
        System.out.println("   - tongTien: " + saved.getTongTien());
        System.out.println("   - tienGiamGia: " + saved.getTienGiamGia());
        System.out.println("   - thanhTien: " + saved.getThanhTien());
        
        // Log activity: UPDATE
        try {
            String newDataJson = serializeHoaDonToJson(saved);
            hoaDonActivityService.logActivity(
                saved,
                "UPDATE",
                String.format("Cập nhật thông tin hóa đơn: %s", saved.getMaHoaDon()),
                oldDataJson,
                newDataJson
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to log UPDATE activity: " + e.getMessage());
        }
        
        // ✅ Xử lý danh sách phiếu giảm giá nếu có (trong updateHoaDon)
        if (dto.getPhieuGiamGiaIds() != null && !dto.getPhieuGiamGiaIds().isEmpty()) {
            // Lưu danh sách ID phiếu giảm giá vào ghiChu dưới dạng JSON
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String phieuGiamGiaIdsJson = objectMapper.writeValueAsString(dto.getPhieuGiamGiaIds());
                String existingGhiChu = saved.getGhiChu() != null ? saved.getGhiChu() : "";
                // Xóa dòng PHIEU_GIAM_GIA_IDS cũ nếu có
                String cleanedGhiChu = existingGhiChu.replaceAll("(?m)^PHIEU_GIAM_GIA_IDS:.*$", "").trim();
                String newGhiChu = cleanedGhiChu.isEmpty() 
                    ? "PHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson
                    : cleanedGhiChu + "\nPHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson;
                saved.setGhiChu(newGhiChu);
                System.out.println("✅ Updated phieuGiamGiaIds in ghiChu: " + phieuGiamGiaIdsJson);
            } catch (Exception e) {
                System.err.println("❌ Error updating phieuGiamGiaIds: " + e.getMessage());
            }
        } else if (dto.getPhieuGiamGiaId() != null) {
            // Giữ tương thích với phieuGiamGiaId cũ
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String phieuGiamGiaIdsJson = objectMapper.writeValueAsString(java.util.Arrays.asList(dto.getPhieuGiamGiaId()));
                String existingGhiChu = saved.getGhiChu() != null ? saved.getGhiChu() : "";
                String cleanedGhiChu = existingGhiChu.replaceAll("(?m)^PHIEU_GIAM_GIA_IDS:.*$", "").trim();
                String newGhiChu = cleanedGhiChu.isEmpty() 
                    ? "PHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson
                    : cleanedGhiChu + "\nPHIEU_GIAM_GIA_IDS: " + phieuGiamGiaIdsJson;
                saved.setGhiChu(newGhiChu);
                System.out.println("✅ Updated phieuGiamGiaId (legacy) in ghiChu: " + phieuGiamGiaIdsJson);
            } catch (Exception e) {
                System.err.println("❌ Error updating phieuGiamGiaId: " + e.getMessage());
            }
        }
        
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
            // ✅ QUAN TRỌNG: Chỉ set trạng thái "Đã Thanh Toán" khi phương thức thanh toán là chuyển khoản
            // Với tiền mặt, trạng thái sẽ là CHUA_THANH_TOAN (mặc định)
            if ("Chuyển khoản".equals(tenHinhThuc) || "transfer".equalsIgnoreCase(phuongThucTen)) {
                pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN);
                System.out.println("✅ Payment method is transfer - Set status to DA_THANH_TOAN");
            } else {
                pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.CHO_THANH_TOAN);
                System.out.println("✅ Payment method is cash - Set status to CHO_THANH_TOAN");
            }
            phuongThucThanhToanRepository.save(pttt);
        }
        
        // ✅ CẬP NHẬT THÔNG TIN ĐỊA CHỈ GIAO HÀNG (ThongTinDonHang) NẾU CÓ THAY ĐỔI
        // Kiểm tra xem có thông tin địa chỉ mới không
        if (dto.getTinhThanh() != null || dto.getQuanHuyen() != null || dto.getPhuongXa() != null || dto.getDiaChiChiTiet() != null) {
            try {
                // Tìm ThongTinDonHang hiện tại
                Optional<ThongTinDonHang> existingThongTin = thongTinDonHangRepository.findByHoaDonId(saved.getId());
                
                if (existingThongTin.isPresent()) {
                    // Cập nhật ThongTinDonHang hiện tại
                    ThongTinDonHang thongTin = existingThongTin.get();
                    if (dto.getTenKhachHang() != null) {
                        thongTin.setTenNguoiNhan(dto.getTenKhachHang());
                    }
                    if (dto.getSoDienThoaiKhachHang() != null) {
                        thongTin.setSoDienThoai(dto.getSoDienThoaiKhachHang());
                    }
                    
                    // Tạo địa chỉ giao hàng đầy đủ từ các thành phần
                    String diaChiChiTiet = dto.getDiaChiChiTiet() != null ? dto.getDiaChiChiTiet() : "";
                    String phuongXa = dto.getPhuongXa() != null ? dto.getPhuongXa() : (thongTin.getPhuongXa() != null ? thongTin.getPhuongXa() : "");
                    String quanHuyen = dto.getQuanHuyen() != null ? dto.getQuanHuyen() : (thongTin.getQuanHuyen() != null ? thongTin.getQuanHuyen() : "");
                    String tinhThanh = dto.getTinhThanh() != null ? dto.getTinhThanh() : (thongTin.getTinhThanh() != null ? thongTin.getTinhThanh() : "");
                    
                    String diaChiGiaoHang = String.format("%s, %s, %s, %s",
                        diaChiChiTiet, phuongXa, quanHuyen, tinhThanh);
                    thongTin.setDiaChiGiaoHang(diaChiGiaoHang.trim().replaceAll("^,\\s*|,\\s*$", ""));
                    
                    if (dto.getTinhThanh() != null) {
                        thongTin.setTinhThanh(dto.getTinhThanh());
                    }
                    if (dto.getQuanHuyen() != null) {
                        thongTin.setQuanHuyen(dto.getQuanHuyen());
                    }
                    if (dto.getPhuongXa() != null) {
                        thongTin.setPhuongXa(dto.getPhuongXa());
                    }
                    if (dto.getGhiChu() != null) {
                        thongTin.setGhiChu(dto.getGhiChu());
                    }
                    
                    // ✅ QUAN TRỌNG: Cập nhật phí vận chuyển nếu có trong DTO
                    if (dto.getPhiGiaoHang() != null) {
                        thongTin.setPhiVanChuyen(dto.getPhiGiaoHang());
                        System.out.println("💰 Updated ThongTinDonHang phiVanChuyen to: " + dto.getPhiGiaoHang());
                    }
                    
                    thongTinDonHangRepository.save(thongTin);
                    System.out.println("✅ Updated ThongTinDonHang for invoice ID: " + saved.getId());
                } else if (dto.getTenKhachHang() != null && dto.getSoDienThoaiKhachHang() != null &&
                          (dto.getDiaChiChiTiet() != null || dto.getTinhThanh() != null)) {
                    // Tạo mới ThongTinDonHang nếu chưa có
                    ThongTinDonHang thongTinDonHang = new ThongTinDonHang();
                    thongTinDonHang.setHoaDon(saved);
                    thongTinDonHang.setTenNguoiNhan(dto.getTenKhachHang());
                    thongTinDonHang.setSoDienThoai(dto.getSoDienThoaiKhachHang());
                    
                    String diaChiGiaoHang = String.format("%s, %s, %s, %s",
                        dto.getDiaChiChiTiet() != null ? dto.getDiaChiChiTiet() : "",
                        dto.getPhuongXa() != null ? dto.getPhuongXa() : "",
                        dto.getQuanHuyen() != null ? dto.getQuanHuyen() : "",
                        dto.getTinhThanh() != null ? dto.getTinhThanh() : "");
                    thongTinDonHang.setDiaChiGiaoHang(diaChiGiaoHang.trim().replaceAll("^,\\s*|,\\s*$", ""));
                    thongTinDonHang.setTinhThanh(dto.getTinhThanh() != null ? dto.getTinhThanh() : "");
                    thongTinDonHang.setQuanHuyen(dto.getQuanHuyen() != null ? dto.getQuanHuyen() : "");
                    thongTinDonHang.setPhuongXa(dto.getPhuongXa() != null ? dto.getPhuongXa() : "");
                    thongTinDonHang.setGhiChu(dto.getGhiChu());
                    
                    // ✅ QUAN TRỌNG: Set phí vận chuyển khi tạo mới
                    if (dto.getPhiGiaoHang() != null) {
                        thongTinDonHang.setPhiVanChuyen(dto.getPhiGiaoHang());
                        System.out.println("💰 Setting new ThongTinDonHang phiVanChuyen to: " + dto.getPhiGiaoHang());
                    }
                    
                    thongTinDonHangRepository.save(thongTinDonHang);
                    System.out.println("✅ Created new ThongTinDonHang for invoice ID: " + saved.getId());
                }
            } catch (Exception e) {
                // Không block việc cập nhật hóa đơn nếu cập nhật ThongTinDonHang thất bại
                System.err.println("⚠️ Could not update ThongTinDonHang: " + e.getMessage());
            }
        }
        
        // Reload hóa đơn để đảm bảo có dữ liệu đầy đủ
        Optional<HoaDon> reloaded = getHoaDonById(saved.getId());
        if (reloaded.isPresent()) {
            HoaDon reloadedHoaDon = reloaded.get();
            
            // ✅ QUAN TRỌNG: Kiểm tra xem địa chỉ có thay đổi không và gửi email cho khách hàng
            try {
                String newAddress = null;
                Optional<ThongTinDonHang> newThongTin = thongTinDonHangRepository.findByHoaDonId(saved.getId());
                if (newThongTin.isPresent()) {
                    newAddress = newThongTin.get().getDiaChiGiaoHang();
                } else if (reloadedHoaDon.getKhachHang() != null && reloadedHoaDon.getKhachHang().getDiaChi() != null) {
                    newAddress = reloadedHoaDon.getKhachHang().getDiaChi();
                }
                
                // So sánh địa chỉ cũ và mới
                boolean addressChanged = false;
                if (oldAddress == null && newAddress != null && !newAddress.trim().isEmpty()) {
                    addressChanged = true; // Từ không có địa chỉ -> có địa chỉ
                } else if (oldAddress != null && newAddress != null && !oldAddress.equals(newAddress)) {
                    addressChanged = true; // Địa chỉ thay đổi
                } else if (oldAddress != null && (newAddress == null || newAddress.trim().isEmpty())) {
                    addressChanged = true; // Từ có địa chỉ -> không có địa chỉ
                }
                
                // Gửi email nếu địa chỉ thay đổi và có thông tin khách hàng
                if (addressChanged && reloadedHoaDon.getKhachHang() != null) {
                    String customerEmail = reloadedHoaDon.getKhachHang().getEmail();
                    String customerName = reloadedHoaDon.getKhachHang().getTenKhachHang();
                    
                    if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                        System.out.println("📧 Sending address update email to: " + customerEmail);
                        System.out.println("   Old address: " + (oldAddress != null ? oldAddress : "N/A"));
                        System.out.println("   New address: " + (newAddress != null ? newAddress : "N/A"));
                        
                        emailService.sendAddressUpdateEmail(
                            customerEmail,
                            customerName,
                            reloadedHoaDon.getMaHoaDon(),
                            oldAddress != null ? oldAddress : "Chưa có địa chỉ",
                            newAddress != null ? newAddress : "Chưa có địa chỉ"
                        );
                    } else {
                        System.out.println("⚠️ Customer email is empty, cannot send address update email");
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send address update email: " + e.getMessage());
                // Không throw exception để không ảnh hưởng đến việc cập nhật hóa đơn
            }
            
            return toDTO(reloadedHoaDon);
        }
        return toDTO(saved);
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        
        // Lưu dữ liệu cũ để log trước khi xóa
        String oldDataJson = null;
        try {
            oldDataJson = serializeHoaDonToJson(h);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to serialize data before delete: " + e.getMessage());
        }
        
        String maHoaDon = h.getMaHoaDon();
        Long hoaDonId = h.getId();
        
        hoaDonRepository.delete(h);
        
        // Log activity: DELETE
        try {
            // Sử dụng logActivity với oldData để lưu thông tin hóa đơn đã xóa
            // Tạo một HoaDon tạm để log (không lưu vào DB)
            HoaDon tempHoaDon = new HoaDon();
            tempHoaDon.setId(hoaDonId);
            tempHoaDon.setMaHoaDon(maHoaDon);
            hoaDonActivityService.logActivity(
                tempHoaDon,
                "DELETE",
                String.format("Xóa hóa đơn: %s", maHoaDon),
                oldDataJson,
                null
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to log DELETE activity: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái hóa đơn
     * QUAN TRỌNG: 
     * - CHỈ cập nhật trạng thái bằng query trực tiếp, KHÔNG động đến entity và danhSachChiTiet
     * - Khi chuyển sang DA_XAC_NHAN (Đã xác nhận), sẽ TRỪ tồn kho sản phẩm
     * - Khi chuyển từ DA_XAC_NHAN sang trạng thái khác (trừ DA_HUY và DA_GIAO_HANG), sẽ HOÀN LẠI tồn kho
     */
    @Transactional
    public HoaDonDTO updateTrangThaiHoaDon(Long id, String trangThai) {
        System.out.println("🔄 updateTrangThaiHoaDon called for invoice ID: " + id + ", new status: " + trangThai);
        
        // Load hoá đơn để lấy trạng thái cũ
        Optional<HoaDon> hoaDonOpt = getHoaDonById(id);
        if (!hoaDonOpt.isPresent()) {
            throw new EntityNotFoundException("Không tìm thấy hóa đơn với ID: " + id);
        }
        HoaDon hoaDon = hoaDonOpt.get();
        HoaDon.TrangThaiHoaDon oldTrangThai = hoaDon.getTrangThai();
        System.out.println("📋 Current status: " + oldTrangThai + " -> New status: " + trangThai);
        
        // QUAN TRỌNG: Verify danhSachChiTiet TRƯỚC khi update (để log)
        List<HoaDonChiTiet> chiTietBeforeUpdate = hoaDonChiTietRepository.findByHoaDonId(id);
        int danhSachChiTietSizeBefore = (chiTietBeforeUpdate != null ? chiTietBeforeUpdate.size() : 0);
        System.out.println("📦 Before update - danhSachChiTiet count in DB: " + danhSachChiTietSizeBefore);
        
        // Validate trạng thái - sử dụng convertStringToTrangThaiEnum để map HUY -> DA_HUY
        try {
            HoaDon.TrangThaiHoaDon newTrangThai = convertStringToTrangThaiEnum(trangThai);
            if (newTrangThai == null) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + trangThai);
            }
            
            // QUAN TRỌNG: Xử lý tồn kho khi cập nhật trạng thái
            // Logic mới:
            // 1. Đơn hàng ONLINE (nhanVienId = null): Đã trừ stock khi tạo đơn, chỉ hoàn lại nếu hủy
            // 2. Đơn hàng TẠI QUẦY (nhanVienId != null): Trừ stock khi xác nhận (DA_XAC_NHAN)
            
            boolean isOnlineOrder = hoaDon.getNhanVien() == null;
            
            // QUAN TRỌNG: Xử lý tồn kho khi cập nhật trạng thái
            // Logic mới:
            // 1. Đơn hàng ONLINE (nhanVienId = null): Đã trừ stock khi đặt hàng thành công (khi tạo đơn), chỉ hoàn lại nếu hủy
            // 2. Đơn hàng TẠI QUẦY (nhanVienId != null): Trừ stock khi admin xác nhận (DA_XAC_NHAN), hoàn lại nếu hủy
            
            if (isOnlineOrder) {
                // Đơn hàng ONLINE - đã trừ stock khi đặt hàng thành công (khi tạo đơn)
                if (newTrangThai == HoaDon.TrangThaiHoaDon.DA_HUY) {
                    // Hủy đơn hàng online - hoàn lại stock (cả khách hàng và admin/nhân viên hủy)
                    if (oldTrangThai != HoaDon.TrangThaiHoaDon.DA_HUY) {
                        System.out.println("💰 Online order cancelled (DA_HUY) - Restoring stock...");
                        System.out.println("   - Old status: " + oldTrangThai + " -> New status: " + newTrangThai);
                        restoreStockFromInvoice(chiTietBeforeUpdate);
                    }
                }
                // Nếu chuyển sang DA_XAC_NHAN hoặc các trạng thái khác, không làm gì (đã trừ stock rồi)
            } else {
                // Đơn hàng TẠI QUẦY - trừ stock khi admin xác nhận
                if (newTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN && 
                    oldTrangThai != HoaDon.TrangThaiHoaDon.DA_XAC_NHAN) {
                    // Chuyển SANG DA_XAC_NHAN: Trừ tồn kho (thanh toán thành công)
                    System.out.println("💰 Counter order confirmed (DA_XAC_NHAN) - Deducting stock (payment successful)...");
                    deductStockFromInvoice(chiTietBeforeUpdate);
                } else if (newTrangThai == HoaDon.TrangThaiHoaDon.DA_HUY) {
                    // Hủy đơn hàng tại quầy - hoàn lại stock nếu đã xác nhận (đã trừ stock)
                    if (oldTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN) {
                        System.out.println("💰 Counter order cancelled (DA_HUY) after confirmation - Restoring stock...");
                        System.out.println("   - Old status: " + oldTrangThai + " -> New status: " + newTrangThai);
                        restoreStockFromInvoice(chiTietBeforeUpdate);
                    } else {
                        System.out.println("💰 Counter order cancelled (DA_HUY) before confirmation - No stock to restore");
                    }
                } else if (oldTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN && 
                           newTrangThai != HoaDon.TrangThaiHoaDon.DA_XAC_NHAN &&
                           newTrangThai != HoaDon.TrangThaiHoaDon.DA_HUY &&
                           newTrangThai != HoaDon.TrangThaiHoaDon.DA_GIAO_HANG &&
                           newTrangThai != HoaDon.TrangThaiHoaDon.DANG_GIAO_HANG) {
                    // Chuyển TỪ DA_XAC_NHAN sang trạng thái khác (trừ DA_HUY, DA_GIAO_HANG, DANG_GIAO_HANG): Hoàn lại tồn kho
                    System.out.println("💰 Counter order status changed from DA_XAC_NHAN to " + newTrangThai + " - Restoring stock...");
                    restoreStockFromInvoice(chiTietBeforeUpdate);
                }
            }
            
            // QUAN TRỌNG: Update trạng thái bằng query trực tiếp, KHÔNG load entity
            // Điều này tránh vấn đề với orphanRemoval và đảm bảo danhSachChiTiet không bị ảnh hưởng
            jakarta.persistence.Query updateQuery = entityManager.createQuery(
                "UPDATE HoaDon h SET h.trangThai = :trangThai WHERE h.id = :id"
            );
            updateQuery.setParameter("trangThai", newTrangThai);
            updateQuery.setParameter("id", id);
            int updatedCount = updateQuery.executeUpdate();
            entityManager.flush(); // Force flush để đảm bảo update được lưu
            
            System.out.println("✅ Updated " + updatedCount + " invoice(s) with new status: " + newTrangThai);
            
            // Verify danhSachChiTiet SAU khi update (để đảm bảo không bị xóa)
            List<HoaDonChiTiet> chiTietAfterUpdate = hoaDonChiTietRepository.findByHoaDonId(id);
            int danhSachChiTietSizeAfter = (chiTietAfterUpdate != null ? chiTietAfterUpdate.size() : 0);
            System.out.println("📦 After update - danhSachChiTiet count in DB: " + danhSachChiTietSizeAfter);
            
            if (danhSachChiTietSizeAfter != danhSachChiTietSizeBefore) {
                System.err.println("❌ CRITICAL ERROR: danhSachChiTiet count changed from " + danhSachChiTietSizeBefore + 
                    " to " + danhSachChiTietSizeAfter + " after status update!");
                System.err.println("   This should NOT happen when only updating status!");
            }
            
            // Clear persistence context để force reload từ DB
            entityManager.clear();
            
            // Reload hóa đơn với đầy đủ relationships
            Optional<HoaDon> reloaded = getHoaDonById(id);
            if (reloaded.isPresent()) {
                HoaDon reloadedHoaDon = reloaded.get();
                int reloadedSize = (reloadedHoaDon.getDanhSachChiTiet() != null ? reloadedHoaDon.getDanhSachChiTiet().size() : 0);
                System.out.println("✅ Reloaded invoice - danhSachChiTiet size: " + reloadedSize);
                
                // Log activity: STATUS_CHANGE
                try {
                    String oldDataJson = serializeHoaDonToJson(hoaDon); // Dữ liệu cũ (trước khi update)
                    String newDataJson = serializeHoaDonToJson(reloadedHoaDon); // Dữ liệu mới (sau khi update)
                    hoaDonActivityService.logActivity(
                        reloadedHoaDon,
                        "STATUS_CHANGE",
                        String.format("Cập nhật trạng thái từ %s sang %s", oldTrangThai, newTrangThai),
                        oldDataJson,
                        newDataJson
                    );
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to log STATUS_CHANGE activity: " + e.getMessage());
                }
                
                // Gửi email thông báo thay đổi trạng thái cho khách hàng
                try {
                    sendInvoiceStatusChangeEmail(reloadedHoaDon, oldTrangThai.toString(), newTrangThai.toString());
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to send status change email notification: " + e.getMessage());
                    // Không throw exception để không ảnh hưởng đến việc cập nhật trạng thái
                }
                
                // Verify lại trong reloaded entity
                if (reloadedSize == 0 && danhSachChiTietSizeBefore > 0) {
                    System.err.println("❌ CRITICAL: danhSachChiTiet is empty in reloaded entity but DB has " + danhSachChiTietSizeBefore + " records!");
                    // Last resort: load từ repository và set vào
                    if (chiTietAfterUpdate != null && !chiTietAfterUpdate.isEmpty()) {
                        System.out.println("🔧 Loading " + chiTietAfterUpdate.size() + " items from repository as last resort");
                        reloadedHoaDon.setDanhSachChiTiet(chiTietAfterUpdate);
                    }
                }
                
                // QUAN TRỌNG: Xử lý hoàn tiền khi hủy đơn hàng đã thanh toán
                if (newTrangThai == HoaDon.TrangThaiHoaDon.DA_HUY && hoaDon.getNgayThanhToan() != null) {
                    // Đơn hàng đã thanh toán và bị hủy - cần hoàn tiền
                    System.out.println("💰 Invoice cancelled and was paid - Refund required");
                    System.out.println("   Payment date: " + hoaDon.getNgayThanhToan());
                    System.out.println("   Total amount: " + hoaDon.getThanhTien());
                    
                    // Ghi chú về hoàn tiền vào ghiChu
                    String refundNote = "\n[HOÀN TIỀN] Đơn hàng đã bị hủy. Số tiền " + hoaDon.getThanhTien() + " sẽ được hoàn trả trong vòng 3-5 ngày làm việc sau khi nhận được thông tin tài khoản ngân hàng.";
                    String updatedGhiChu = (reloadedHoaDon.getGhiChu() != null ? reloadedHoaDon.getGhiChu() : "") + refundNote;
                    jakarta.persistence.Query updateGhiChuQuery = entityManager.createQuery(
                        "UPDATE HoaDon h SET h.ghiChu = :ghiChu WHERE h.id = :id"
                    );
                    updateGhiChuQuery.setParameter("ghiChu", updatedGhiChu);
                    updateGhiChuQuery.setParameter("id", id);
                    updateGhiChuQuery.executeUpdate();
                    
                    // Cập nhật trạng thái thanh toán thành DA_HOAN_TIEN nếu có PhuongThucThanhToan
                    List<PhuongThucThanhToan> ptttList = phuongThucThanhToanRepository.findByHoaDonId(id);
                    boolean isTransferPayment = false; // Flag để kiểm tra phương thức thanh toán
                    
                    if (!ptttList.isEmpty()) {
                        for (PhuongThucThanhToan pttt : ptttList) {
                            if (pttt.getTrangThai() == PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN) {
                                pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_HOAN_TIEN);
                                
                                // Kiểm tra phương thức thanh toán
                                if (pttt.getHinhThucThanhToan() != null) {
                                    String tenHinhThuc = pttt.getHinhThucThanhToan().getTenHinhThuc();
                                    // Chỉ yêu cầu thông tin tài khoản cho đơn hàng chuyển khoản
                                    if ("Chuyển khoản".equals(tenHinhThuc) || "Chuyển Khoản".equals(tenHinhThuc)) {
                                        isTransferPayment = true;
                                        pttt.setGhiChu("Hoàn tiền do hủy đơn hàng - Đang chờ thông tin tài khoản");
                                    } else {
                                        // Đơn hàng tiền mặt - không cần thông tin tài khoản
                                        pttt.setGhiChu("Hoàn tiền do hủy đơn hàng - Sẽ hoàn tiền tại cửa hàng");
                                    }
                                }
                                
                                phuongThucThanhToanRepository.save(pttt);
                            }
                        }
                    }
                    
                    // ✅ CHỈ GỬI EMAIL VỚI FORM REFUND CHO ĐƠN HÀNG THANH TOÁN BẰNG CHUYỂN KHOẢN
                    // Đơn hàng tiền mặt sẽ được hoàn tiền tại cửa hàng, không cần form
                    if (isTransferPayment) {
                        // Gửi email yêu cầu thông tin hoàn tiền cho khách hàng (chỉ cho đơn hàng chuyển khoản)
                        try {
                            if (reloadedHoaDon.getKhachHang() != null) {
                                String customerEmail = reloadedHoaDon.getKhachHang().getEmail();
                                String customerName = reloadedHoaDon.getKhachHang().getTenKhachHang();
                                
                                if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                                    System.out.println("📧 Sending refund request email (transfer payment) to: " + customerEmail);
                                    // Tạo link để khách hàng nhập thông tin tài khoản
                                    String refundLink = "http://localhost:4200/refund?invoice=" + java.net.URLEncoder.encode(reloadedHoaDon.getMaHoaDon(), java.nio.charset.StandardCharsets.UTF_8);
                                    emailService.sendRefundRequestEmail(
                                        customerEmail,
                                        customerName,
                                        reloadedHoaDon.getMaHoaDon(),
                                        reloadedHoaDon.getThanhTien(),
                                        refundLink
                                    );
                                    System.out.println("✅ Refund request email sent successfully to: " + customerEmail);
                                } else {
                                    System.out.println("⚠️ Customer email is empty, skipping refund request email");
                                }
                            } else {
                                System.out.println("⚠️ Invoice has no customer, skipping refund request email");
                            }
                        } catch (Exception e) {
                            System.err.println("⚠️ Failed to send refund request email: " + e.getMessage());
                            e.printStackTrace();
                            // Không throw exception để không ảnh hưởng đến việc hủy đơn hàng
                        }
                    } else {
                        // Đơn hàng tiền mặt - không gửi email với form, chỉ ghi chú
                        System.out.println("ℹ️ Invoice payment method is cash - No refund form email needed. Customer will receive refund at store.");
                    }
                }
                
                return toDTO(reloadedHoaDon);
            }
            
            throw new EntityNotFoundException("Không thể reload hóa đơn sau khi cập nhật trạng thái");
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + trangThai);
        }
    }

    /**
     * Xử lý hoàn tiền khi hủy đơn hàng
     * @param id Invoice ID
     * @param refundRequest Thông tin hoàn tiền
     * @return Updated invoice DTO
     */
    @Transactional
    public HoaDonDTO processRefund(Long id, com.example.backend.dto.RefundRequest refundRequest) {
        System.out.println("💰 Processing refund for invoice ID: " + id);
        
        Optional<HoaDon> hoaDonOpt = getHoaDonById(id);
        if (!hoaDonOpt.isPresent()) {
            throw new EntityNotFoundException("Không tìm thấy hóa đơn với ID: " + id);
        }
        
        HoaDon hoaDon = hoaDonOpt.get();
        
        // Kiểm tra đơn hàng đã thanh toán chưa
        if (hoaDon.getNgayThanhToan() == null) {
            throw new IllegalStateException("Đơn hàng chưa thanh toán, không cần hoàn tiền");
        }
        
        // Kiểm tra đơn hàng đã bị hủy chưa
        if (hoaDon.getTrangThai() != HoaDon.TrangThaiHoaDon.DA_HUY) {
            throw new IllegalStateException("Chỉ có thể hoàn tiền cho đơn hàng đã bị hủy");
        }
        
        // Tính số tiền cần hoàn (mặc định là thanhTien nếu không chỉ định)
        java.math.BigDecimal refundAmount = refundRequest.getRefundAmount();
        if (refundAmount == null || refundAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            refundAmount = hoaDon.getThanhTien();
        }
        
        // Cập nhật trạng thái thanh toán thành DA_HOAN_TIEN
        List<PhuongThucThanhToan> ptttList = phuongThucThanhToanRepository.findByHoaDonId(id);
        if (!ptttList.isEmpty()) {
            for (PhuongThucThanhToan pttt : ptttList) {
                if (pttt.getTrangThai() == PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN) {
                    pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_HOAN_TIEN);
                    String refundNote = String.format("Hoàn tiền: %s. Lý do: %s. Phương thức: %s", 
                        refundAmount, 
                        refundRequest.getRefundReason() != null ? refundRequest.getRefundReason() : "Hủy đơn hàng",
                        refundRequest.getRefundMethod() != null ? refundRequest.getRefundMethod() : "original_method");
                    pttt.setGhiChu(refundNote);
                    phuongThucThanhToanRepository.save(pttt);
                }
            }
        }
        
        // Ghi chú hoàn tiền vào ghiChu của hóa đơn
        String refundNote = String.format("\n[HOÀN TIỀN] Số tiền: %s. Lý do: %s. Phương thức: %s. Thời gian: %s", 
            refundAmount,
            refundRequest.getRefundReason() != null ? refundRequest.getRefundReason() : "Hủy đơn hàng",
            refundRequest.getRefundMethod() != null ? refundRequest.getRefundMethod() : "original_method",
            java.time.LocalDateTime.now());
        String updatedGhiChu = (hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "") + refundNote;
        
        jakarta.persistence.Query updateGhiChuQuery = entityManager.createQuery(
            "UPDATE HoaDon h SET h.ghiChu = :ghiChu WHERE h.id = :id"
        );
        updateGhiChuQuery.setParameter("ghiChu", updatedGhiChu);
        updateGhiChuQuery.setParameter("id", id);
        updateGhiChuQuery.executeUpdate();
        
        // Log activity
        try {
            hoaDonActivityService.logActivity(
                hoaDon,
                "REFUND",
                String.format("Hoàn tiền: %s. Lý do: %s", refundAmount, refundRequest.getRefundReason()),
                null,
                null
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to log REFUND activity: " + e.getMessage());
        }
        
        // Reload và return
        Optional<HoaDon> reloaded = getHoaDonById(id);
        if (reloaded.isPresent()) {
            return toDTO(reloaded.get());
        }
        
        throw new EntityNotFoundException("Không thể reload hóa đơn sau khi hoàn tiền");
    }

    /**
     * Điều chỉnh phí ship (hoàn phí hoặc tăng phụ phí)
     * @param id Invoice ID
     * @param adjustmentRequest Thông tin điều chỉnh
     * @return Updated invoice DTO
     */
    @Transactional
    public HoaDonDTO adjustShippingFee(Long id, com.example.backend.dto.ShippingFeeAdjustmentRequest adjustmentRequest) {
        System.out.println("🚚 Adjusting shipping fee for invoice ID: " + id);
        
        Optional<HoaDon> hoaDonOpt = getHoaDonById(id);
        if (!hoaDonOpt.isPresent()) {
            throw new EntityNotFoundException("Không tìm thấy hóa đơn với ID: " + id);
        }
        
        HoaDon hoaDon = hoaDonOpt.get();
        
        // Tính toán chênh lệch phí ship
        java.math.BigDecimal oldFee = adjustmentRequest.getOldShippingFee() != null 
            ? adjustmentRequest.getOldShippingFee() 
            : java.math.BigDecimal.ZERO;
        java.math.BigDecimal newFee = adjustmentRequest.getNewShippingFee() != null 
            ? adjustmentRequest.getNewShippingFee() 
            : java.math.BigDecimal.ZERO;
        java.math.BigDecimal difference = newFee.subtract(oldFee);
        
        // Cập nhật phí ship mới (cần lưu vào ThongTinDonHang hoặc cập nhật trực tiếp)
        // Tạm thời ghi vào ghiChu và tính lại thanhTien
        
        // ✅ QUAN TRỌNG: Cập nhật phiGiaoHang vào DB
        jakarta.persistence.Query updatePhiGiaoHangQuery = entityManager.createQuery(
            "UPDATE HoaDon h SET h.phiGiaoHang = :phiGiaoHang WHERE h.id = :id"
        );
        updatePhiGiaoHangQuery.setParameter("phiGiaoHang", newFee);
        updatePhiGiaoHangQuery.setParameter("id", id);
        updatePhiGiaoHangQuery.executeUpdate();
        System.out.println("✅ Updated phiGiaoHang in DB: " + newFee);
        
        // Tính lại thanhTien với phí ship mới
        java.math.BigDecimal newThanhTien = hoaDon.getTongTien()
            .subtract(hoaDon.getTienGiamGia() != null ? hoaDon.getTienGiamGia() : java.math.BigDecimal.ZERO)
            .add(newFee);
        
        // Cập nhật thanhTien
        jakarta.persistence.Query updateThanhTienQuery = entityManager.createQuery(
            "UPDATE HoaDon h SET h.thanhTien = :thanhTien WHERE h.id = :id"
        );
        updateThanhTienQuery.setParameter("thanhTien", newThanhTien);
        updateThanhTienQuery.setParameter("id", id);
        updateThanhTienQuery.executeUpdate();
        System.out.println("✅ Updated thanhTien in DB: " + newThanhTien);
        
        // Ghi chú điều chỉnh phí ship
        String adjustmentNote;
        if (difference.compareTo(java.math.BigDecimal.ZERO) > 0) {
            // Tăng phụ phí
            adjustmentNote = String.format("\n[TĂNG PHỤ PHÍ SHIP] Phí ship cũ: %s, Phí ship mới: %s, Tăng thêm: %s. Lý do: %s", 
                oldFee, newFee, difference, 
                adjustmentRequest.getReason() != null ? adjustmentRequest.getReason() : "Thay đổi địa chỉ giao hàng");
        } else if (difference.compareTo(java.math.BigDecimal.ZERO) < 0) {
            // Hoàn phí
            adjustmentNote = String.format("\n[HOÀN PHÍ SHIP] Phí ship cũ: %s, Phí ship mới: %s, Hoàn lại: %s. Lý do: %s. Phương thức hoàn: %s", 
                oldFee, newFee, difference.abs(), 
                adjustmentRequest.getReason() != null ? adjustmentRequest.getReason() : "Thay đổi địa chỉ giao hàng",
                adjustmentRequest.getRefundMethod() != null ? adjustmentRequest.getRefundMethod() : "original_method");
        } else {
            adjustmentNote = String.format("\n[ĐIỀU CHỈNH PHÍ SHIP] Phí ship không thay đổi: %s. Lý do: %s", 
                newFee,
                adjustmentRequest.getReason() != null ? adjustmentRequest.getReason() : "Cập nhật thông tin");
        }
        
        String updatedGhiChu = (hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "") + adjustmentNote;
        
        jakarta.persistence.Query updateGhiChuQuery = entityManager.createQuery(
            "UPDATE HoaDon h SET h.ghiChu = :ghiChu WHERE h.id = :id"
        );
        updateGhiChuQuery.setParameter("ghiChu", updatedGhiChu);
        updateGhiChuQuery.setParameter("id", id);
        updateGhiChuQuery.executeUpdate();
        
        // Log activity
        try {
            hoaDonActivityService.logActivity(
                hoaDon,
                "SHIPPING_FEE_ADJUSTMENT",
                String.format("Điều chỉnh phí ship: %s -> %s (%s: %s)", 
                    oldFee, newFee, 
                    difference.compareTo(java.math.BigDecimal.ZERO) > 0 ? "Tăng" : "Hoàn",
                    difference.abs()),
                null,
                null
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to log SHIPPING_FEE_ADJUSTMENT activity: " + e.getMessage());
        }
        
        // Reload và return
        Optional<HoaDon> reloaded = getHoaDonById(id);
        if (reloaded.isPresent()) {
            return toDTO(reloaded.get());
        }
        
        throw new EntityNotFoundException("Không thể reload hóa đơn sau khi điều chỉnh phí ship");
    }

    /**
     * Trừ tồn kho sản phẩm
     * - Đối với đơn hàng ONLINE: Được gọi ngay khi tạo đơn hàng (khách hàng đã thanh toán)
     * - Đối với đơn hàng TẠI QUẦY: Được gọi khi admin/staff xác nhận đơn hàng (status = DA_XAC_NHAN)
     */
    private void deductStockFromInvoice(List<HoaDonChiTiet> danhSachChiTiet) {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            System.out.println("⚠️ No items to deduct stock from");
            return;
        }
        
        System.out.println("📦 Deducting stock for " + danhSachChiTiet.size() + " items...");
        
        for (HoaDonChiTiet chiTiet : danhSachChiTiet) {
            if (chiTiet.getChiTietSanPham() == null || chiTiet.getChiTietSanPham().getId() == null) {
                System.out.println("⚠️ Skipping item with null chiTietSanPham or chiTietSanPham.id");
                continue;
            }
            
            Long chiTietSanPhamId = chiTiet.getChiTietSanPham().getId();
            
            // Load lại ChiTietSanPham từ DB để đảm bảo có dữ liệu mới nhất về tồn kho
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                .orElse(null);
            
            if (chiTietSanPham == null) {
                System.err.println("❌ ChiTietSanPham not found for id: " + chiTietSanPhamId);
                continue;
            }
            
            int requestedQuantity = chiTiet.getSoLuong();
            if (requestedQuantity <= 0) {
                System.out.println("⚠️ Skipping item with invalid quantity: " + requestedQuantity);
                continue;
            }
            
            int currentStock = 0;
            try {
                currentStock = Integer.parseInt(chiTietSanPham.getSoLuongTon());
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Invalid stock format for ChiTietSanPham id: " + chiTietSanPhamId + 
                    ", soLuongTon: " + chiTietSanPham.getSoLuongTon());
                continue;
            }
            
            // Trừ tồn kho
            int newStock = currentStock - requestedQuantity;
            if (newStock < 0) {
                System.err.println("❌ WARNING: Stock would be negative for ChiTietSanPham id: " + 
                    chiTietSanPhamId + " (current: " + currentStock + ", requested: " + requestedQuantity + ")");
                newStock = 0; // Đặt về 0 thay vì âm
            }
            
            // Update tồn kho trong database
            chiTietSanPham.setSoLuongTon(String.valueOf(newStock));
            chiTietSanPhamRepository.save(chiTietSanPham);
            chiTietSanPhamRepository.flush();
            
            System.out.println("✅ Deducted stock for ChiTietSanPham id: " + chiTietSanPhamId + 
                " - " + requestedQuantity + " units (from " + currentStock + " to " + newStock + ")");
        }
        
        System.out.println("✅ Stock deduction completed");
    }
    
    /**
     * Hoàn lại tồn kho sản phẩm khi hoá đơn chuyển TỪ DA_XAC_NHAN sang trạng thái khác
     * (Không hoàn lại nếu chuyển sang DA_HUY, DA_GIAO_HANG, DANG_GIAO_HANG vì đơn đang tiến triển)
     */
    private void restoreStockFromInvoice(List<HoaDonChiTiet> danhSachChiTiet) {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            System.out.println("⚠️ No items to restore stock for");
            return;
        }
        
        System.out.println("📦 Restoring stock for " + danhSachChiTiet.size() + " items...");
        
        for (HoaDonChiTiet chiTiet : danhSachChiTiet) {
            if (chiTiet.getChiTietSanPham() == null || chiTiet.getChiTietSanPham().getId() == null) {
                System.out.println("⚠️ Skipping item with null chiTietSanPham or chiTietSanPham.id");
                continue;
            }
            
            Long chiTietSanPhamId = chiTiet.getChiTietSanPham().getId();
            
            // Load lại ChiTietSanPham từ DB để đảm bảo có dữ liệu mới nhất về tồn kho
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                .orElse(null);
            
            if (chiTietSanPham == null) {
                System.err.println("❌ ChiTietSanPham not found for id: " + chiTietSanPhamId);
                continue;
            }
            
            int quantityToRestore = chiTiet.getSoLuong();
            if (quantityToRestore <= 0) {
                System.out.println("⚠️ Skipping item with invalid quantity: " + quantityToRestore);
                continue;
            }
            
            int currentStock = 0;
            try {
                currentStock = Integer.parseInt(chiTietSanPham.getSoLuongTon());
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Invalid stock format for ChiTietSanPham id: " + chiTietSanPhamId + 
                    ", soLuongTon: " + chiTietSanPham.getSoLuongTon());
                continue;
            }
            
            // Hoàn lại tồn kho
            int newStock = currentStock + quantityToRestore;
            chiTietSanPham.setSoLuongTon(String.valueOf(newStock));
            chiTietSanPhamRepository.save(chiTietSanPham);
            chiTietSanPhamRepository.flush();
            
            System.out.println("✅ Restored stock for ChiTietSanPham id: " + chiTietSanPhamId + 
                " - +" + quantityToRestore + " units (from " + currentStock + " to " + newStock + ")");
        }
        
        System.out.println("✅ Stock restoration completed");
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

    /**
     * Lấy hóa đơn theo mã hóa đơn
     */
    public Optional<HoaDon> getHoaDonByMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return Optional.empty();
        }
        return hoaDonRepository.findByMaHoaDon(maHoaDon.trim());
    }

    public Optional<HoaDon> getHoaDonById(Long id) {
        // QUAN TRỌNG: Sử dụng EntityGraph hoặc query với JOIN FETCH để đảm bảo load danhSachChiTiet
        // Cũng load thêm loaiMuBaoHiem cho sản phẩm
        // QUAN TRỌNG: Load cho TẤT CẢ các trạng thái, không filter theo trạng thái
        System.out.println("🔍 getHoaDonById - Loading invoice ID: " + id);
        jakarta.persistence.TypedQuery<HoaDon> query = entityManager.createQuery(
            "SELECT DISTINCT h FROM HoaDon h " +
            "LEFT JOIN FETCH h.khachHang " +
            "LEFT JOIN FETCH h.nhanVien " +
            "LEFT JOIN FETCH h.danhSachChiTiet c " +
            "LEFT JOIN FETCH c.chiTietSanPham ct " +
            "LEFT JOIN FETCH ct.sanPham s " +
            "LEFT JOIN FETCH s.nhaSanXuat " +
            "LEFT JOIN FETCH s.loaiMuBaoHiem " +
            "LEFT JOIN FETCH ct.mauSac " +
            "LEFT JOIN FETCH ct.kichThuoc " +
            "WHERE h.id = :id",
            HoaDon.class
        );
        query.setParameter("id", id);
        
        try {
            HoaDon hoaDon = query.getSingleResult();
            System.out.println("✅ Loaded HoaDon ID: " + id + ", status: " + hoaDon.getTrangThai() + ", soLuongSanPham: " + hoaDon.getSoLuongSanPham());
            System.out.println("💰 [getHoaDonById] phiGiaoHang from DB (hoa_don.phi_giao_hang): " + hoaDon.getPhiGiaoHang());
            
            // QUAN TRỌNG: Luôn kiểm tra và load danhSachChiTiet cho TẤT CẢ các trạng thái
            // Không chỉ load cho CHO_XAC_NHAN mà load cho tất cả
            if (hoaDon.getDanhSachChiTiet() != null && !hoaDon.getDanhSachChiTiet().isEmpty()) {
                System.out.println("✅ Loaded danhSachChiTiet with " + hoaDon.getDanhSachChiTiet().size() + " items from JOIN FETCH");
                // Force load để đảm bảo không bị LazyInitializationException
                hoaDon.getDanhSachChiTiet().forEach(item -> {
                    if (item.getChiTietSanPham() != null) {
                        if (item.getChiTietSanPham().getSanPham() != null) {
                            // Force load sanPham và các relationships
                            item.getChiTietSanPham().getSanPham().getTenSanPham();
                        }
                    }
                });
            } else {
                System.out.println("⚠️ danhSachChiTiet is null or empty in getHoaDonById for ID: " + id + ", status: " + hoaDon.getTrangThai());
                System.out.println("   soLuongSanPham: " + hoaDon.getSoLuongSanPham());
                // QUAN TRỌNG: Luôn thử load lại từ repository cho TẤT CẢ các trạng thái
                // Không chỉ load cho CHO_XAC_NHAN mà load cho tất cả
                System.out.println("🔄 Attempting to load danhSachChiTiet from repository for ALL statuses...");
                List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(id);
                if (chiTietList != null && !chiTietList.isEmpty()) {
                    System.out.println("✅ Found " + chiTietList.size() + " items in repository for status " + hoaDon.getTrangThai() + ", setting to hoaDon");
                    hoaDon.setDanhSachChiTiet(chiTietList);
                } else {
                    System.out.println("⚠️ No danhSachChiTiet found in repository for invoice ID: " + id);
                    System.out.println("   Status: " + hoaDon.getTrangThai());
                    System.out.println("   soLuongSanPham: " + hoaDon.getSoLuongSanPham());
                    System.out.println("   This may indicate data inconsistency or the items were not saved properly");
                    
                    // Thử simple query nếu query phức tạp không hoạt động
                    System.out.println("🔄 Trying simple query...");
                    try {
                        List<HoaDonChiTiet> simpleResults = hoaDonChiTietRepository.findByHoaDonIdSimple(id);
                        if (simpleResults != null && !simpleResults.isEmpty()) {
                            System.out.println("✅ Found " + simpleResults.size() + " items with simple query!");
                            hoaDon.setDanhSachChiTiet(simpleResults);
                        } else {
                            System.out.println("❌ Still no items found with simple query");
                            hoaDon.setDanhSachChiTiet(new java.util.ArrayList<>());
                        }
                    } catch (Exception e2) {
                        System.err.println("❌ Error in simple query: " + e2.getMessage());
                        hoaDon.setDanhSachChiTiet(new java.util.ArrayList<>());
                    }
                }
            }
            
            // Log kết quả cuối cùng
            System.out.println("📤 Final result - Invoice ID: " + id + ", danhSachChiTiet size: " + 
                              (hoaDon.getDanhSachChiTiet() != null ? hoaDon.getDanhSachChiTiet().size() : "null"));
            
            return Optional.of(hoaDon);
        } catch (jakarta.persistence.NoResultException e) {
            System.err.println("❌ No HoaDon found with ID: " + id);
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("❌ Error loading HoaDon by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Page<HoaDon> getHoaDonByKhachHangId(Long khachHangId, Pageable pageable) {
        // QUAN TRỌNG: Hiển thị TẤT CẢ đơn hàng của khách hàng, TRỪ các đơn hàng đã hủy (DA_HUY/HUY)
        // Khách hàng cần thấy cả đơn hàng đang chờ xác nhận (CHO_XAC_NHAN) để theo dõi trạng thái
        // Chỉ loại bỏ các đơn hàng đã hủy (DA_HUY/HUY)
        // Đếm tổng số bản ghi (trừ các đơn hàng đã hủy)
        jakarta.persistence.TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(DISTINCT h) FROM HoaDon h " +
            "WHERE h.khachHang.id = :khachHangId " +
            "AND h.trangThai != 'DA_HUY' " +
            "AND h.trangThai != 'HUY'",
            Long.class
        );
        countQuery.setParameter("khachHangId", khachHangId);
        long totalElements = countQuery.getSingleResult();
        
        System.out.println("📋 getHoaDonByKhachHangId - Total orders (excluding cancelled) for customer " + khachHangId + ": " + totalElements);
        
        // Query với join fetch để load các relationships - lấy tất cả đơn hàng trừ đã hủy
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
            "AND h.trangThai != 'DA_HUY' " +
            "AND h.trangThai != 'HUY' " +
            "ORDER BY h.ngayTao DESC",
            HoaDon.class
        );
        query.setParameter("khachHangId", khachHangId);
        
        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<HoaDon> results = query.getResultList();
        
        System.out.println("📋 getHoaDonByKhachHangId - Returning " + results.size() + " orders for page " + pageable.getPageNumber());
        
        // Create a Page manually
        return new org.springframework.data.domain.PageImpl<>(results, pageable, totalElements);
    }

    /**
     * Convert TrangThaiHoaDon enum sang String cho DTO
     * Map DA_HUY -> HUY cho frontend
     */
    private String convertTrangThaiEnumToString(HoaDon.TrangThaiHoaDon trangThai) {
        if (trangThai == null) {
            return null;
        }
        // Map DA_HUY sang HUY cho frontend
        if (trangThai == HoaDon.TrangThaiHoaDon.DA_HUY) {
            return "HUY";
        }
        return trangThai.name();
    }

    /**
     * Convert String từ DTO sang TrangThaiHoaDon enum
     * Map HUY -> DA_HUY cho backend
     */
    private HoaDon.TrangThaiHoaDon convertStringToTrangThaiEnum(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty()) {
            return null;
        }
        // Map HUY từ frontend sang DA_HUY cho backend
        if ("HUY".equals(trangThai)) {
            return HoaDon.TrangThaiHoaDon.DA_HUY;
        }
        try {
            return HoaDon.TrangThaiHoaDon.valueOf(trangThai);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + trangThai, e);
        }
    }

    /**
     * Serialize HoaDon entity thành JSON string để lưu vào oldData/newData
     */
    private String serializeHoaDonToJson(HoaDon hoaDon) {
        if (hoaDon == null) {
            return null;
        }
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            
            // Thông tin cơ bản
            jsonNode.put("id", hoaDon.getId() != null ? hoaDon.getId() : 0);
            jsonNode.put("maHoaDon", hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon() : "");
            jsonNode.put("trangThai", hoaDon.getTrangThai() != null ? hoaDon.getTrangThai().name() : "");
            jsonNode.put("tongTien", hoaDon.getTongTien() != null ? hoaDon.getTongTien().toString() : "0");
            jsonNode.put("tienGiamGia", hoaDon.getTienGiamGia() != null ? hoaDon.getTienGiamGia().toString() : "0");
            jsonNode.put("thanhTien", hoaDon.getThanhTien() != null ? hoaDon.getThanhTien().toString() : "0");
            jsonNode.put("ghiChu", hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "");
            jsonNode.put("soLuongSanPham", hoaDon.getSoLuongSanPham() != null ? hoaDon.getSoLuongSanPham() : 0);
            
            // Thông tin khách hàng
            if (hoaDon.getKhachHang() != null) {
                jsonNode.put("khachHangId", hoaDon.getKhachHang().getId() != null ? hoaDon.getKhachHang().getId() : 0);
                jsonNode.put("tenKhachHang", hoaDon.getKhachHang().getTenKhachHang() != null ? hoaDon.getKhachHang().getTenKhachHang() : "");
            }
            
            // Thông tin nhân viên
            if (hoaDon.getNhanVien() != null) {
                jsonNode.put("nhanVienId", hoaDon.getNhanVien().getId() != null ? hoaDon.getNhanVien().getId() : 0);
                jsonNode.put("tenNhanVien", hoaDon.getNhanVien().getHoTen() != null ? hoaDon.getNhanVien().getHoTen() : "");
            }
            
            // Thông tin ngày tháng
            if (hoaDon.getNgayTao() != null) {
                jsonNode.put("ngayTao", hoaDon.getNgayTao().toString());
            }
            if (hoaDon.getNgayThanhToan() != null) {
                jsonNode.put("ngayThanhToan", hoaDon.getNgayThanhToan().toString());
            }
            
            // Số lượng chi tiết
            int chiTietCount = 0;
            if (hoaDon.getDanhSachChiTiet() != null) {
                chiTietCount = hoaDon.getDanhSachChiTiet().size();
            }
            jsonNode.put("soLuongChiTiet", chiTietCount);
            
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            System.err.println("⚠️ Error serializing HoaDon to JSON: " + e.getMessage());
            // Fallback: return simple string representation
            return String.format("{\"id\":%d,\"maHoaDon\":\"%s\",\"trangThai\":\"%s\"}",
                hoaDon.getId() != null ? hoaDon.getId() : 0,
                hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon() : "",
                hoaDon.getTrangThai() != null ? hoaDon.getTrangThai().name() : "");
        }
    }

    /**
     * Gửi email thông báo hóa đơn cho khách hàng khi tạo hóa đơn
     */
    private void sendInvoiceEmailNotification(HoaDonDTO dto, HoaDon entity) {
        try {
            // Lấy thông tin khách hàng - ưu tiên từ entity, sau đó từ DTO
            String customerEmail = null;
            String customerName = null;
            
            // Ưu tiên lấy từ entity (đã được load đầy đủ từ DB)
            if (entity.getKhachHang() != null) {
                customerEmail = entity.getKhachHang().getEmail();
                customerName = entity.getKhachHang().getTenKhachHang();
            }
            
            // Nếu không có từ entity, lấy từ DTO
            if ((customerEmail == null || customerEmail.trim().isEmpty()) && dto.getEmailKhachHang() != null) {
                customerEmail = dto.getEmailKhachHang();
            }
            if ((customerName == null || customerName.trim().isEmpty()) && dto.getTenKhachHang() != null) {
                customerName = dto.getTenKhachHang();
            }
            
            System.out.println("📧 Preparing to send invoice email notification:");
            System.out.println("   - Customer Email: " + customerEmail);
            System.out.println("   - Customer Name: " + customerName);
            System.out.println("   - Invoice Code: " + dto.getMaHoaDon());
            
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                System.out.println("⚠️ Customer email is empty, skipping email notification");
                System.out.println("   - Entity has customer: " + (entity.getKhachHang() != null));
                if (entity.getKhachHang() != null) {
                    System.out.println("   - Entity customer email: " + entity.getKhachHang().getEmail());
                }
                System.out.println("   - DTO email: " + dto.getEmailKhachHang());
                return;
            }
            
            // Tạo danh sách sản phẩm
            List<EmailService.InvoiceItemInfo> danhSachSanPham = new ArrayList<>();
            if (entity.getDanhSachChiTiet() != null && !entity.getDanhSachChiTiet().isEmpty()) {
                for (HoaDonChiTiet chiTiet : entity.getDanhSachChiTiet()) {
                    String tenSanPham = "N/A";
                    if (chiTiet.getChiTietSanPham() != null && chiTiet.getChiTietSanPham().getSanPham() != null) {
                        tenSanPham = chiTiet.getChiTietSanPham().getSanPham().getTenSanPham();
                    }
                    
                    EmailService.InvoiceItemInfo item = new EmailService.InvoiceItemInfo(
                        tenSanPham,
                        chiTiet.getSoLuong(),
                        chiTiet.getDonGia(),
                        chiTiet.getThanhTien()
                    );
                    danhSachSanPham.add(item);
                }
            }
            
            // Tạo địa chỉ giao hàng đầy đủ từ các thành phần
            StringBuilder diaChiBuilder = new StringBuilder();
            if (dto.getDiaChiChiTiet() != null && !dto.getDiaChiChiTiet().trim().isEmpty()) {
                diaChiBuilder.append(dto.getDiaChiChiTiet());
            }
            if (dto.getPhuongXa() != null && !dto.getPhuongXa().trim().isEmpty()) {
                if (diaChiBuilder.length() > 0) diaChiBuilder.append(", ");
                diaChiBuilder.append(dto.getPhuongXa());
            }
            if (dto.getQuanHuyen() != null && !dto.getQuanHuyen().trim().isEmpty()) {
                if (diaChiBuilder.length() > 0) diaChiBuilder.append(", ");
                diaChiBuilder.append(dto.getQuanHuyen());
            }
            if (dto.getTinhThanh() != null && !dto.getTinhThanh().trim().isEmpty()) {
                if (diaChiBuilder.length() > 0) diaChiBuilder.append(", ");
                diaChiBuilder.append(dto.getTinhThanh());
            }
            String diaChiGiaoHang = diaChiBuilder.length() > 0 ? diaChiBuilder.toString() : 
                (dto.getDiaChiKhachHang() != null ? dto.getDiaChiKhachHang() : "N/A");
            
            // Gửi email
            emailService.sendInvoiceNotification(
                customerEmail,
                customerName,
                dto.getMaHoaDon(),
                dto.getTrangThai(),
                dto.getTongTien(),
                dto.getThanhTien(),
                dto.getNgayTao(),
                diaChiGiaoHang,
                danhSachSanPham
            );
            
            System.out.println("✅ Invoice email notification sent to: " + customerEmail);
        } catch (Exception e) {
            System.err.println("❌ Error sending invoice email notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi email thông báo thay đổi trạng thái hóa đơn cho khách hàng
     */
    private void sendInvoiceStatusChangeEmail(HoaDon hoaDon, String oldStatus, String newStatus) {
        try {
            System.out.println("📧 Preparing to send status change email notification:");
            System.out.println("   - Invoice Code: " + hoaDon.getMaHoaDon());
            System.out.println("   - Old Status: " + oldStatus);
            System.out.println("   - New Status: " + newStatus);
            
            // Lấy thông tin khách hàng
            if (hoaDon.getKhachHang() == null) {
                System.out.println("⚠️ Invoice has no customer, skipping status change email");
                return;
            }
            
            String customerEmail = hoaDon.getKhachHang().getEmail();
            String customerName = hoaDon.getKhachHang().getTenKhachHang();
            
            System.out.println("   - Customer Email: " + customerEmail);
            System.out.println("   - Customer Name: " + customerName);
            
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                System.out.println("⚠️ Customer email is empty, skipping status change email");
                System.out.println("   - Customer ID: " + hoaDon.getKhachHang().getId());
                System.out.println("   - Customer Name: " + customerName);
                return;
            }
            
            // ✅ QUAN TRỌNG: Lấy phương thức thanh toán để hiển thị message phù hợp khi hủy đơn
            String phuongThucThanhToan = null;
            try {
                List<PhuongThucThanhToan> ptttList = phuongThucThanhToanRepository.findByHoaDonId(hoaDon.getId());
                System.out.println("   - Found " + (ptttList != null ? ptttList.size() : 0) + " payment method(s)");
                
                if (ptttList != null && !ptttList.isEmpty()) {
                    PhuongThucThanhToan pttt = ptttList.get(0);
                    if (pttt.getHinhThucThanhToan() != null) {
                        // Lấy tên hình thức thanh toán từ DB (có thể là "Chuyển khoản", "Chuyển Khoản", "Tiền mặt", etc.)
                        String tenHinhThuc = pttt.getHinhThucThanhToan().getTenHinhThuc();
                        phuongThucThanhToan = tenHinhThuc; // Giữ nguyên giá trị từ DB để kiểm tra chính xác
                        System.out.println("   - Payment Method (from DB): '" + phuongThucThanhToan + "'");
                        System.out.println("   - Payment Method (raw object): " + pttt.getHinhThucThanhToan());
                        
                        // Log để debug
                        if (tenHinhThuc != null) {
                            String lower = tenHinhThuc.toLowerCase().trim();
                            boolean isChuyenKhoan = lower.contains("chuyển khoản") || 
                                                   lower.contains("chuyen khoan") || 
                                                   lower.equals("transfer");
                            System.out.println("   - Is Transfer Payment (check): " + isChuyenKhoan);
                        }
                    } else {
                        System.out.println("   - ⚠️ HinhThucThanhToan is null in PhuongThucThanhToan");
                    }
                } else {
                    System.out.println("   - ⚠️ No payment method found for invoice ID: " + hoaDon.getId());
                    System.out.println("   - This might be an unpaid order or payment method not set yet");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to get payment method: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Gửi email với phương thức thanh toán
            System.out.println("📤 Sending status change email to: " + customerEmail);
            emailService.sendInvoiceStatusChangeNotification(
                customerEmail,
                customerName,
                hoaDon.getMaHoaDon(),
                oldStatus,
                newStatus,
                hoaDon.getThanhTien(),
                phuongThucThanhToan
            );
            
            System.out.println("✅ Status change email notification sent successfully to: " + customerEmail);
        } catch (Exception e) {
            System.err.println("❌ Error sending status change email notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
