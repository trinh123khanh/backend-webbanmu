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
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
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
                .trangThai(convertTrangThaiEnumToString(h.getTrangThai()))
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
                List<HoaDonChiTiet> chiTietFromRepo = hoaDonChiTietRepository.findByHoaDonId(h.getId());
                if (chiTietFromRepo != null && !chiTietFromRepo.isEmpty()) {
                    System.out.println("✅ Found " + chiTietFromRepo.size() + " items in repository, using them");
                    chiTietListToMap = chiTietFromRepo;
                    // Set vào entity để lần sau không phải query lại
                    h.setDanhSachChiTiet(chiTietFromRepo);
                } else {
                    System.out.println("⚠️ No danhSachChiTiet found in repository either for invoice ID: " + h.getId());
                    System.out.println("   Invoice soLuongSanPham: " + h.getSoLuongSanPham());
                    System.out.println("   Invoice status: " + h.getTrangThai());
                    // Set empty list thay vì null để frontend có thể xử lý
                    builder.danhSachChiTiet(new java.util.ArrayList<>());
                    chiTietListToMap = null; // Đánh dấu là không có gì
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading danhSachChiTiet from repository: " + e.getMessage());
                e.printStackTrace();
                // Set empty list thay vì null để frontend có thể xử lý
                builder.danhSachChiTiet(new java.util.ArrayList<>());
                chiTietListToMap = null; // Đánh dấu là không có gì
            }
        }
        
        // Map sang DTO nếu có dữ liệu
        if (chiTietListToMap != null && !chiTietListToMap.isEmpty()) {
            System.out.println("📦 Mapping " + chiTietListToMap.size() + " danhSachChiTiet items to DTO...");
            List<HoaDonChiTietDTO> chiTietDTOList = chiTietListToMap.stream()
                    .map(this::toChiTietDTO)
                    .collect(Collectors.toList());
            builder.danhSachChiTiet(chiTietDTOList);
            System.out.println("✅ Mapped danhSachChiTiet, DTO count: " + chiTietDTOList.size());
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
        
        KhachHang attachedCustomer = resolveCustomerForInvoice(d);
        if (attachedCustomer != null) {
            h.setKhachHang(attachedCustomer);
        }
        
        // Map nhân viên từ ID
        if (d.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(d.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + d.getNhanVienId()));
            h.setNhanVien(nhanVien);
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

        if (!StringUtils.hasText(name) || !StringUtils.hasText(phone) || !StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Thông tin khách hàng chưa đầy đủ. Vui lòng cung cấp họ tên, số điện thoại và email.");
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

        KhachHang newKhachHang = new KhachHang();
        newKhachHang.setMaKhachHang(generateUniqueCustomerCode());
        newKhachHang.setTenKhachHang(name);
        newKhachHang.setEmail(email);
        newKhachHang.setSoDienThoai(phone);
        newKhachHang.setDiaChi(sanitize(dto.getDiaChiChiTiet()));
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

        String phone = sanitize(dto.getSoDienThoaiKhachHang());
        if (StringUtils.hasText(phone)) {
            if (matchedByPhone || !StringUtils.hasText(khachHang.getSoDienThoai())) {
                khachHang.setSoDienThoai(phone);
            }
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
                    continue; // Bỏ qua nếu không có chiTietSanPhamId
                }
                
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietDTO.getChiTietSanPhamId()));
                
                // QUAN TRỌNG: Kiểm tra tồn kho trước khi tạo hóa đơn
                int requestedQuantity = chiTietDTO.getSoLuong() != null ? chiTietDTO.getSoLuong() : 0;
                int currentStock = 0;
                try {
                    currentStock = Integer.parseInt(chiTietSanPham.getSoLuongTon());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid stock quantity format for ChiTietSanPham id: " + chiTietSanPham.getId());
                }
                
                if (requestedQuantity > currentStock) {
                    throw new RuntimeException(
                        String.format("Sản phẩm \"%s\" chỉ còn %d sản phẩm trong kho (bạn yêu cầu %d).", 
                            chiTietDTO.getTenSanPham() != null ? chiTietDTO.getTenSanPham() : "N/A",
                            currentStock, requestedQuantity)
                    );
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
            
            // QUAN TRỌNG: KHÔNG trừ số lượng khi tạo hoá đơn
            // Số lượng sẽ CHỈ được trừ khi hoá đơn được xác nhận (status = DA_XAC_NHAN)
            // Điều này áp dụng cho cả đơn hàng online và đơn hàng tại quầy
            // Khách hàng chỉ được coi là "thanh toán thành công" khi admin/staff xác nhận đơn hàng
            System.out.println("✅ Invoice created. Stock will be deducted when status changes to DA_XAC_NHAN (confirmed)");
            
            // Tính lại tổng tiền từ danh sách chi tiết
            if (h.getTongTien() == null || h.getTongTien().compareTo(recalculatedTotal) != 0) {
                System.out.println("⚠️ Recalculated total: " + h.getTongTien() + " -> " + recalculatedTotal);
                h.setTongTien(recalculatedTotal);
                h.setThanhTien(recalculatedTotal.subtract(h.getTienGiamGia() != null ? h.getTienGiamGia() : java.math.BigDecimal.ZERO).max(java.math.BigDecimal.ZERO));
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
        if (saved.getDanhSachChiTiet() != null) {
            System.out.println("✅ Saved invoice with " + saved.getDanhSachChiTiet().size() + " danhSachChiTiet items");
        } else {
            System.err.println("❌ WARNING: danhSachChiTiet is null after save! Invoice ID: " + saved.getId());
            // Thử load lại từ repository
            List<HoaDonChiTiet> chiTietFromRepo = hoaDonChiTietRepository.findByHoaDonId(saved.getId());
            if (chiTietFromRepo != null && !chiTietFromRepo.isEmpty()) {
                System.out.println("✅ Found " + chiTietFromRepo.size() + " items in repository after save");
                saved.setDanhSachChiTiet(chiTietFromRepo);
            }
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
                
                thongTinDonHangRepository.save(thongTinDonHang);
                System.out.println("✅ Created ThongTinDonHang for invoice ID: " + saved.getId());
            } catch (Exception e) {
                // Không block việc tạo hóa đơn nếu tạo ThongTinDonHang thất bại
                System.err.println("⚠️ Could not save ThongTinDonHang: " + e.getMessage());
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
            pttt.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.DA_THANH_TOAN);
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
        if (reloaded.isPresent()) {
            return toDTO(reloaded.get());
        }
        return toDTO(saved);
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
        
        updateEntityFromDTO(h, dto);
        
        // Xử lý danh sách chi tiết sản phẩm nếu có
        // QUAN TRỌNG: Với orphanRemoval = true, KHÔNG được set collection mới hoặc clear() mà không add lại ngay
        // Giải pháp: Chỉ clear và add lại khi có danhSachChiTiet mới (không empty)
        // Nếu danhSachChiTiet là null hoặc empty, giữ nguyên collection hiện tại
        System.out.println("📦 Processing danhSachChiTiet in updateHoaDon:");
        System.out.println("   - dto.getDanhSachChiTiet() is null: " + (dto.getDanhSachChiTiet() == null));
        System.out.println("   - dto.getDanhSachChiTiet() size: " + (dto.getDanhSachChiTiet() != null ? dto.getDanhSachChiTiet().size() : "null"));
        System.out.println("   - Current h.getDanhSachChiTiet() size: " + (h.getDanhSachChiTiet() != null ? h.getDanhSachChiTiet().size() : "null"));

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
            List<HoaDonChiTiet> chiTietList = new ArrayList<>();
            for (HoaDonChiTietDTO chiTietDTO : dto.getDanhSachChiTiet()) {
                if (chiTietDTO.getChiTietSanPhamId() == null) {
                    System.out.println("⚠️ Skipping item with null chiTietSanPhamId");
                    continue; // Bỏ qua nếu không có chiTietSanPhamId


            
//             // Ngay lập tức add các chi tiết mới vào collection (không được để collection rỗng quá lâu)
//             if (!dto.getDanhSachChiTiet().isEmpty()) {
//                 for (HoaDonChiTietDTO chiTietDTO : dto.getDanhSachChiTiet()) {
//                     if (chiTietDTO.getChiTietSanPhamId() == null) {
//                         continue; // Bỏ qua nếu không có chiTietSanPhamId
//                     }
                    
//                     ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
//                             .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietDTO.getChiTietSanPhamId()));
                    
//                     HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
//                     hoaDonChiTiet.setHoaDon(h);
//                     hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
//                     hoaDonChiTiet.setSoLuong(chiTietDTO.getSoLuong() != null ? chiTietDTO.getSoLuong() : 0);
//                     hoaDonChiTiet.setDonGia(chiTietDTO.getDonGia() != null ? chiTietDTO.getDonGia() : java.math.BigDecimal.ZERO);
//                     hoaDonChiTiet.setGiamGia(chiTietDTO.getGiamGia() != null ? chiTietDTO.getGiamGia() : java.math.BigDecimal.ZERO);
//                     hoaDonChiTiet.setThanhTien(chiTietDTO.getThanhTien() != null ? chiTietDTO.getThanhTien() : java.math.BigDecimal.ZERO);
                    
//                     // Add ngay vào collection sau khi clear (không được delay)
//                     h.getDanhSachChiTiet().add(hoaDonChiTiet);

// >>>>>>> main
                }
            }

            System.out.println("✅ Added " + chiTietList.size() + " items to danhSachChiTiet");
        } else {
            // Nếu danhSachChiTiet là null hoặc empty, giữ nguyên collection hiện tại
            // Điều này đảm bảo không mất dữ liệu khi chỉ cập nhật trạng thái hoặc ghi chú
            System.out.println("⚠️ danhSachChiTiet is null or empty, keeping existing collection (size: " +
                (h.getDanhSachChiTiet() != null ? h.getDanhSachChiTiet().size() : 0) + ")");
        }
        
        System.out.println("💾 Saving invoice with ghiChu: '" + h.getGhiChu() + "'");

        // Lưu hóa đơn
        HoaDon saved = hoaDonRepository.save(h);
        
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
        
        // Validate trạng thái
        try {
            HoaDon.TrangThaiHoaDon newTrangThai = HoaDon.TrangThaiHoaDon.valueOf(trangThai);
            
            // QUAN TRỌNG: Xử lý tồn kho TRƯỚC khi update trạng thái
            // Số lượng CHỈ được trừ khi hoá đơn được xác nhận (status = DA_XAC_NHAN)
            // Điều này áp dụng cho CẢ đơn hàng online và đơn hàng tại quầy
            // Khách hàng chỉ được coi là "thanh toán thành công" khi admin/staff xác nhận đơn hàng
            // 1. Nếu chuyển SANG DA_XAC_NHAN: Trừ tồn kho (thanh toán thành công)
            // 2. Nếu chuyển TỪ DA_XAC_NHAN sang trạng thái khác (không phải DA_HUY và DA_GIAO_HANG): Hoàn lại tồn kho
            //    (Nếu chuyển từ DA_XAC_NHAN sang DA_GIAO_HANG, KHÔNG hoàn lại vì đã trừ rồi và đơn đang tiến triển)
            if (newTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN && 
                oldTrangThai != HoaDon.TrangThaiHoaDon.DA_XAC_NHAN) {
                // Chuyển SANG DA_XAC_NHAN: Trừ tồn kho (khách hàng đã thanh toán thành công)
                System.out.println("💰 Status changed to DA_XAC_NHAN (Payment confirmed) - Deducting stock...");
                deductStockFromInvoice(chiTietBeforeUpdate);
            } else if (oldTrangThai == HoaDon.TrangThaiHoaDon.DA_XAC_NHAN && 
                       newTrangThai != HoaDon.TrangThaiHoaDon.DA_XAC_NHAN &&
                       newTrangThai != HoaDon.TrangThaiHoaDon.DA_HUY &&
                       newTrangThai != HoaDon.TrangThaiHoaDon.DA_GIAO_HANG &&
                       newTrangThai != HoaDon.TrangThaiHoaDon.DANG_GIAO_HANG) {
                // Chuyển TỪ DA_XAC_NHAN sang trạng thái khác (trừ DA_HUY, DA_GIAO_HANG, DANG_GIAO_HANG): Hoàn lại tồn kho
                // Chỉ hoàn lại nếu chuyển về CHO_XAC_NHAN (hủy xác nhận)
                System.out.println("💰 Status changed from DA_XAC_NHAN to " + newTrangThai + " - Restoring stock...");
                restoreStockFromInvoice(chiTietBeforeUpdate);
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
                
                // Verify lại trong reloaded entity
                if (reloadedSize == 0 && danhSachChiTietSizeBefore > 0) {
                    System.err.println("❌ CRITICAL: danhSachChiTiet is empty in reloaded entity but DB has " + danhSachChiTietSizeBefore + " records!");
                    // Last resort: load từ repository và set vào
                    if (chiTietAfterUpdate != null && !chiTietAfterUpdate.isEmpty()) {
                        System.out.println("🔧 Loading " + chiTietAfterUpdate.size() + " items from repository as last resort");
                        reloadedHoaDon.setDanhSachChiTiet(chiTietAfterUpdate);
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
     * Trừ tồn kho sản phẩm khi hoá đơn được xác nhận (status = DA_XAC_NHAN)
     * Điều này xảy ra khi admin/staff xác nhận đơn hàng = khách hàng đã thanh toán thành công
     * Áp dụng cho CẢ đơn hàng online và đơn hàng tại quầy
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

    public Optional<HoaDon> getHoaDonById(Long id) {
        // QUAN TRỌNG: Sử dụng EntityGraph hoặc query với JOIN FETCH để đảm bảo load danhSachChiTiet
        // Cũng load thêm loaiMuBaoHiem cho sản phẩm
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
            // Force initialize danhSachChiTiet nếu nó là lazy proxy
            if (hoaDon.getDanhSachChiTiet() != null) {
                System.out.println("✅ Loaded danhSachChiTiet with " + hoaDon.getDanhSachChiTiet().size() + " items");
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
                System.out.println("⚠️ danhSachChiTiet is null in getHoaDonById for ID: " + id);
                // Thử load lại từ repository
                List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(id);
                if (chiTietList != null && !chiTietList.isEmpty()) {
                    System.out.println("✅ Found " + chiTietList.size() + " items in repository, setting to hoaDon");
                    hoaDon.setDanhSachChiTiet(chiTietList);
                }
            }
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
}
