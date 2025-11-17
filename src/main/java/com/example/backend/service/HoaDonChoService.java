package com.example.backend.service;

import com.example.backend.dto.HoaDonChoRequest;
import com.example.backend.dto.HoaDonChoResponse;
import com.example.backend.dto.GioHangChoItemRequest;
import com.example.backend.dto.GioHangChoItemResponse;
import com.example.backend.entity.HoaDonCho;
import com.example.backend.entity.GioHangCho;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.ChiTietSanPham;
import com.example.backend.repository.HoaDonChoRepository;
import com.example.backend.repository.GioHangChoRepository;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.ChiTietSanPhamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class HoaDonChoService {

    private final HoaDonChoRepository hoaDonChoRepository;
    private final GioHangChoRepository gioHangChoRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public HoaDonChoService(HoaDonChoRepository hoaDonChoRepository,
                           GioHangChoRepository gioHangChoRepository,
                           KhachHangRepository khachHangRepository,
                           NhanVienRepository nhanVienRepository,
                           ChiTietSanPhamRepository chiTietSanPhamRepository) {
        this.hoaDonChoRepository = hoaDonChoRepository;
        this.gioHangChoRepository = gioHangChoRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
    }

    public HoaDonChoResponse toResponse(HoaDonCho h) {
        log.debug("Converting HoaDonCho to response. ID: {}, gioHangCho size: {}", 
                h.getId(), h.getGioHangCho() != null ? h.getGioHangCho().size() : 0);
        
        List<GioHangChoItemResponse> gioHang = h.getGioHangCho() != null ?
                h.getGioHangCho().stream()
                        .map(this::toGioHangItemResponse)
                        .collect(Collectors.toList()) :
                List.of();
        
        log.debug("Response gioHang size: {}", gioHang.size());

        Long tongSoLuong = gioHang.stream()
                .mapToLong(item -> item.getSoLuong() != null ? item.getSoLuong() : 0L)
                .sum();

        BigDecimal tongTien = gioHang.stream()
                .map(item -> item.getDonGia() != null && item.getSoLuong() != null ?
                        item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tongGiamGia = gioHang.stream()
                .map(item -> item.getGiamGia() != null ? item.getGiamGia() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal thanhTien = tongTien.subtract(tongGiamGia).max(BigDecimal.ZERO);

        return HoaDonChoResponse.builder()
                .id(h.getId())
                .maHoaDonCho(h.getMaHoaDonCho())
                .khachHangId(h.getKhachHang() != null ? h.getKhachHang().getId() : null)
                .tenKhachHang(h.getTenKhachHang())
                .soDienThoaiKhachHang(h.getSoDienThoaiKhachHang())
                .nhanVienId(h.getNhanVien() != null ? h.getNhanVien().getId() : null)
                .tenNhanVien(h.getTenNhanVien())
                .ghiChu(h.getGhiChu())
                .trangThai(h.getTrangThai())
                .ngayTao(h.getNgayTao())
                .ngayCapNhat(h.getNgayCapNhat())
                .danhSachGioHang(gioHang)
                .tongSoLuong(tongSoLuong)
                .tongTien(tongTien)
                .tongGiamGia(tongGiamGia)
                .thanhTien(thanhTien)
                .build();
    }

    private GioHangChoItemResponse toGioHangItemResponse(GioHangCho item) {
        return GioHangChoItemResponse.builder()
                .id(item.getId())
                .chiTietSanPhamId(item.getChiTietSanPham() != null ? item.getChiTietSanPham().getId() : null)
                .tenSanPham(item.getTenSanPham())
                .soLuong(item.getSoLuong())
                .donGia(item.getDonGia())
                .giamGia(item.getGiamGia())
                .thanhTien(item.getThanhTien())
                .build();
    }

    @Transactional
    public HoaDonChoResponse createHoaDonCho(HoaDonChoRequest request) {
        // Kiểm tra số lượng hóa đơn chờ hiện tại (tối đa 5)
        final int MAX_PENDING_INVOICES = 10;
        long countPending = hoaDonChoRepository.countByTrangThai("DANG_CHO");
        if (countPending >= MAX_PENDING_INVOICES) {
            throw new RuntimeException(
                String.format("Bạn chỉ có thể tạo tối đa %d hóa đơn chờ. Vui lòng xóa hoặc thanh toán hóa đơn chờ hiện tại trước khi tạo mới.", 
                    MAX_PENDING_INVOICES)
            );
        }

        HoaDonCho hoaDonCho = new HoaDonCho();
        hoaDonCho.setMaHoaDonCho(request.getMaHoaDonCho());
        hoaDonCho.setTenKhachHang(request.getTenKhachHang());
        hoaDonCho.setSoDienThoaiKhachHang(request.getSoDienThoaiKhachHang());
        hoaDonCho.setTenNhanVien(request.getTenNhanVien());
        hoaDonCho.setGhiChu(request.getGhiChu());
        hoaDonCho.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : "DANG_CHO");

        // Set khachHang if khachHangId is provided
        if (request.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                    .orElse(null);
            hoaDonCho.setKhachHang(khachHang);
        }

        // Set nhanVien if nhanVienId is provided
        if (request.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getNhanVienId())
                    .orElse(null);
            hoaDonCho.setNhanVien(nhanVien);
        }

        HoaDonCho saved = hoaDonChoRepository.save(hoaDonCho);

        // Add cart items if provided
        if (request.getDanhSachGioHang() != null && !request.getDanhSachGioHang().isEmpty()) {
            for (GioHangChoItemRequest itemRequest : request.getDanhSachGioHang()) {
                addItemToCart(saved.getId(), itemRequest);
            }
        }

        // Reload with fresh data using fetch join
        return toResponse(hoaDonChoRepository.findByIdWithGioHangCho(saved.getId()).orElseThrow());
    }

    @Transactional
    public HoaDonChoResponse addItemToCart(Long hoaDonChoId, GioHangChoItemRequest itemRequest) {
        HoaDonCho hoaDonCho = hoaDonChoRepository.findById(hoaDonChoId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn chờ với ID: " + hoaDonChoId));

        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(itemRequest.getChiTietSanPhamId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chi tiết sản phẩm với ID: " + itemRequest.getChiTietSanPhamId()));

        int quantityToAdd = itemRequest.getSoLuong() != null ? itemRequest.getSoLuong() : 1;

        BigDecimal requestedPrice = resolveDonGia(itemRequest, chiTietSanPham);

        List<GioHangCho> existingItems = gioHangChoRepository
                .findAllByHoaDonChoIdAndChiTietSanPhamId(hoaDonChoId, itemRequest.getChiTietSanPhamId());

        int totalReservedQuantity = existingItems.stream()
                .map(GioHangCho::getSoLuong)
                .filter(q -> q != null && q > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int currentStock = parseSoLuongTon(chiTietSanPham);
        int stockAvailable = currentStock - totalReservedQuantity;
        if (quantityToAdd > stockAvailable) {
            throw new RuntimeException(
                String.format("Số lượng sản phẩm không đủ. Hiện tại còn %d sản phẩm trong kho.", Math.max(stockAvailable, 0))
            );
        }

        Optional<GioHangCho> existingItemSamePrice = existingItems.stream()
                .filter(item -> isSamePrice(item.getDonGia(), requestedPrice))
                .findFirst();

        GioHangCho gioHangCho;
        if (existingItemSamePrice.isPresent()) {
            gioHangCho = existingItemSamePrice.get();
            gioHangCho.setSoLuong(gioHangCho.getSoLuong() + quantityToAdd);
        } else {
            gioHangCho = new GioHangCho();
            gioHangCho.setHoaDonCho(hoaDonCho);
            gioHangCho.setChiTietSanPham(chiTietSanPham);
            gioHangCho.setTenSanPham(itemRequest.getTenSanPham());
            gioHangCho.setSoLuong(quantityToAdd);
            gioHangCho.setDonGia(requestedPrice);
            gioHangCho.setGiamGia(itemRequest.getGiamGia() != null ? itemRequest.getGiamGia() : BigDecimal.ZERO);
        }
        
        // Calculate thanhTien
        BigDecimal total = gioHangCho.getDonGia().multiply(BigDecimal.valueOf(gioHangCho.getSoLuong()));
        gioHangCho.setThanhTien(total.subtract(gioHangCho.getGiamGia() != null ? gioHangCho.getGiamGia() : BigDecimal.ZERO).max(BigDecimal.ZERO));

        gioHangChoRepository.save(gioHangCho);
        gioHangChoRepository.flush();

        // QUAN TRỌNG: KHÔNG trừ số lượng khi thêm vào giỏ hàng
        // Số lượng sẽ được trừ khi:
        // 1. Tạo hoá đơn từ giỏ hàng (tại quầy) - khi tạo HoaDon từ HoaDonCho
        // 2. Tạo hoá đơn từ website (checkout) - khi tạo HoaDon với status = DA_XAC_NHAN
        // 3. Cập nhật trạng thái hoá đơn thành DA_XAC_NHAN
        // Chỉ kiểm tra tồn kho để đảm bảo đủ hàng, nhưng không trừ ngay
        log.info("✅ Added item to cart. ChiTietSanPham id: {}, quantity: {}, current stock: {} (Stock will be deducted when invoice is created or confirmed)", 
                chiTietSanPham.getId(), quantityToAdd, currentStock);

        // Reload with fresh data using fetch join
        return toResponse(hoaDonChoRepository.findByIdWithGioHangCho(hoaDonChoId).orElseThrow());
    }

    private int parseSoLuongTon(ChiTietSanPham chiTietSanPham) {
        int currentStock = 0;
        try {
            currentStock = Integer.parseInt(chiTietSanPham.getSoLuongTon());
        } catch (NumberFormatException e) {
            log.warn("Invalid stock quantity format for ChiTietSanPham id: {}", chiTietSanPham.getId());
        }
        return currentStock;
    }

    private boolean isSamePrice(BigDecimal price1, BigDecimal price2) {
        if (price1 == null || price2 == null) {
            return false;
        }
        return price1.compareTo(price2) == 0;
    }

    private BigDecimal resolveDonGia(GioHangChoItemRequest itemRequest, ChiTietSanPham chiTietSanPham) {
        if (itemRequest.getDonGia() != null) {
            return itemRequest.getDonGia();
        }
        if (chiTietSanPham.getGiaBan() != null) {
            try {
                return new BigDecimal(chiTietSanPham.getGiaBan());
            } catch (NumberFormatException e) {
                log.warn("Invalid giaBan format for ChiTietSanPham id: {}", chiTietSanPham.getId());
            }
        }
        return BigDecimal.ZERO;
    }

    @Transactional
    public HoaDonChoResponse updateCartItemQuantity(Long hoaDonChoId, Long gioHangChoId, Integer soLuong) {
        GioHangCho gioHangCho = gioHangChoRepository.findById(gioHangChoId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giỏ hàng với ID: " + gioHangChoId));

        if (!gioHangCho.getHoaDonCho().getId().equals(hoaDonChoId)) {
            throw new RuntimeException("Giỏ hàng không thuộc hóa đơn chờ này");
        }

        int newQuantity = soLuong != null && soLuong > 0 ? soLuong : 1;
        int oldQuantity = gioHangCho.getSoLuong();
        int quantityDifference = newQuantity - oldQuantity;

        // Get product and current stock
        ChiTietSanPham chiTietSanPham = gioHangCho.getChiTietSanPham();
        int currentStock = 0;
        try {
            currentStock = Integer.parseInt(chiTietSanPham.getSoLuongTon());
        } catch (NumberFormatException e) {
            log.warn("Invalid stock quantity format for ChiTietSanPham id: {}", chiTietSanPham.getId());
        }

        // If increasing quantity, check if stock is sufficient
        if (quantityDifference > 0 && quantityDifference > currentStock) {
            throw new RuntimeException(
                String.format("Số lượng sản phẩm không đủ. Hiện tại còn %d sản phẩm trong kho.", currentStock)
            );
        }

        // Update cart quantity
        gioHangCho.setSoLuong(newQuantity);
        
        // Recalculate thanhTien
        BigDecimal total = gioHangCho.getDonGia().multiply(BigDecimal.valueOf(gioHangCho.getSoLuong()));
        gioHangCho.setThanhTien(total.subtract(gioHangCho.getGiamGia() != null ? gioHangCho.getGiamGia() : BigDecimal.ZERO).max(BigDecimal.ZERO));

        gioHangChoRepository.save(gioHangCho);
        gioHangChoRepository.flush();

        // QUAN TRỌNG: KHÔNG trừ/cộng số lượng khi cập nhật số lượng trong giỏ hàng
        // Số lượng sẽ được trừ khi tạo hoá đơn từ giỏ hàng hoặc khi hoá đơn được xác nhận
        // Chỉ kiểm tra tồn kho để đảm bảo đủ hàng, nhưng không trừ ngay
        log.info("✅ Updated cart item quantity. ChiTietSanPham id: {}, quantity changed from {} to {}, current stock: {} (Stock will be deducted when invoice is created or confirmed)", 
                chiTietSanPham.getId(), oldQuantity, newQuantity, currentStock);

        // Reload with fresh data using fetch join
        return toResponse(hoaDonChoRepository.findByIdWithGioHangCho(hoaDonChoId).orElseThrow());
    }

    @Transactional
    public HoaDonChoResponse removeItemFromCart(Long hoaDonChoId, Long gioHangChoId) {
        log.info("Deleting cart item. hoaDonChoId: {}, gioHangChoId: {}", hoaDonChoId, gioHangChoId);
        
        // Verify the cart item exists and belongs to this invoice
        GioHangCho gioHangCho = gioHangChoRepository.findById(gioHangChoId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giỏ hàng với ID: " + gioHangChoId));

        if (!gioHangCho.getHoaDonCho().getId().equals(hoaDonChoId)) {
            throw new RuntimeException("Giỏ hàng không thuộc hóa đơn chờ này");
        }

        log.info("Found cart item to delete: id={}, name={}", gioHangCho.getId(), gioHangCho.getTenSanPham());
        
        // Get product and quantity (for logging only)
        ChiTietSanPham chiTietSanPham = gioHangCho.getChiTietSanPham();
        int quantityRemoved = gioHangCho.getSoLuong();
        
        // Use native query to delete directly from database - this bypasses JPA caching
        // This ensures immediate deletion in database
        try {
            gioHangChoRepository.deleteByIdNative(gioHangChoId);
            gioHangChoRepository.flush();
            log.info("Cart item deleted from database using native query and flushed.");
        } catch (Exception e) {
            log.error("Native delete failed: {}", e.getMessage(), e);
            // Fallback to JPA delete
            gioHangChoRepository.delete(gioHangCho);
            gioHangChoRepository.flush();
            log.info("Fallback to JPA delete successful.");
        }

        // QUAN TRỌNG: KHÔNG hoàn lại số lượng khi xóa khỏi giỏ hàng
        // Vì số lượng chưa bị trừ khi thêm vào giỏ hàng
        // Số lượng chỉ được trừ khi tạo hoá đơn từ giỏ hàng hoặc khi hoá đơn được xác nhận
        log.info("✅ Removed item from cart. ChiTietSanPham id: {}, quantity removed: {} (Stock was not deducted when added to cart, so no need to restore)", 
                chiTietSanPham.getId(), quantityRemoved);

        // Clear entity manager to force fresh load from database
        entityManager.clear();

        // Reload HoaDonCho with fresh data from database using fetch join
        HoaDonCho reloaded = hoaDonChoRepository.findByIdWithGioHangCho(hoaDonChoId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn chờ với ID: " + hoaDonChoId));
        
        log.info("HoaDonCho reloaded. gioHangCho size after delete: {}", 
                reloaded.getGioHangCho() != null ? reloaded.getGioHangCho().size() : 0);
        
        return toResponse(reloaded);
    }

    @Transactional
    public HoaDonChoResponse updateHoaDonCho(Long id, HoaDonChoRequest request) {
        HoaDonCho hoaDonCho = hoaDonChoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn chờ với ID: " + id));

        if (request.getTenKhachHang() != null) {
            hoaDonCho.setTenKhachHang(request.getTenKhachHang());
        }
        if (request.getSoDienThoaiKhachHang() != null) {
            hoaDonCho.setSoDienThoaiKhachHang(request.getSoDienThoaiKhachHang());
        }
        if (request.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                    .orElse(null);
            hoaDonCho.setKhachHang(khachHang);
        }
        if (request.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getNhanVienId())
                    .orElse(null);
            hoaDonCho.setNhanVien(nhanVien);
        }
        if (request.getTenNhanVien() != null) {
            hoaDonCho.setTenNhanVien(request.getTenNhanVien());
        }
        if (request.getGhiChu() != null) {
            hoaDonCho.setGhiChu(request.getGhiChu());
        }
        if (request.getTrangThai() != null) {
            hoaDonCho.setTrangThai(request.getTrangThai());
        }

        hoaDonChoRepository.save(hoaDonCho);

        return toResponse(hoaDonCho);
    }

    @Transactional
    public void deleteHoaDonCho(Long id) {
        HoaDonCho hoaDonCho = hoaDonChoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn chờ với ID: " + id));
        hoaDonChoRepository.delete(hoaDonCho);
    }

    public List<HoaDonChoResponse> getAllHoaDonCho() {
        return hoaDonChoRepository.findAllOrderByNgayTaoDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HoaDonChoResponse> getHoaDonChoByTrangThai(String trangThai) {
        return hoaDonChoRepository.findByTrangThai(trangThai).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HoaDonChoResponse getHoaDonChoById(Long id) {
        return hoaDonChoRepository.findByIdWithGioHangCho(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn chờ với ID: " + id));
    }

    public List<HoaDonChoResponse> getHoaDonChoByKhachHangId(Long khachHangId) {
        return hoaDonChoRepository.findByKhachHangId(khachHangId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

